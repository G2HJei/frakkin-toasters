package xyz.zlatanov.frakkintoasters.backend.live;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import xyz.zlatanov.frakkintoasters.projection.Viewer;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * One browser's web socket to a game, as one viewer. It sends from its own queue with one writer, so a slow browser
 * holds up only itself, never the game. One that falls behind is closed, and can reconnect.
 */
@Slf4j
public final class LiveConnection {

    static final int     MAX_QUEUED = 512;
    static final Integer CLOSED     = null;

    private final WebSocketSession      session;
    private final UUID                  gameBundleId;
    private final UUID                  userId;
    private final Viewer                viewer;
    private final BlockingQueue<Outgoing> queue = new LinkedBlockingQueue<>(MAX_QUEUED);
    private final Thread                  writer;
    // when the client last sent anything: it pings while it has nothing else to say
    private volatile long                 heardAt = System.nanoTime();

    // what the writer takes from the queue: a message, or the close that follows the messages queued before it
    private sealed interface Outgoing {
    }

    private record Text(String json) implements Outgoing {
    }

    private record Close(CloseStatus status) implements Outgoing {
    }

    LiveConnection(WebSocketSession session, UUID gameBundleId, UUID userId, Viewer viewer) {
        this.session = session;
        this.gameBundleId = gameBundleId;
        this.userId = userId;
        this.viewer = viewer;
        this.writer = Thread.ofVirtual().name("ws-" + session.getId()).start(this::write);
    }

    public UUID gameBundleId() {
        return gameBundleId;
    }

    public UUID userId() {
        return userId;
    }

    public Viewer viewer() {
        return viewer;
    }

    void heard() {
        heardAt = System.nanoTime();
    }

    Duration silence() {
        return Duration.ofNanos(System.nanoTime() - heardAt);
    }

    /**
     * Queues a message. Never blocks: a connection whose queue is full is closed.
     */
    public void send(String json) {
        if (!queue.offer(new Text(json))) {
            log.warn("Connection {} fell behind, closing it", session.getId());
            close(CloseStatus.SESSION_NOT_RELIABLE.withReason("Fell behind"));
        }
    }

    /**
     * Closes at once, dropping what is still queued.
     */
    public void close(CloseStatus status) {
        writer.interrupt();
        closeSession(status);
    }

    /**
     * Closes once the messages queued before have gone out, e.g. the rejection that says why.
     */
    public void closeAfterSending(CloseStatus status) {
        if (!queue.offer(new Close(status))) {
            close(status);
        }
    }

    private void closeSession(CloseStatus status) {
        try {
            if (session.isOpen()) {
                session.close(status);
            }
        } catch (IOException e) {
            log.debug("Closing {} failed", session.getId(), e);
        }
    }

    // The container's send has a time limit: a send that hangs fails, and the connection closes.
    private void write() {
        try {
            while (session.isOpen()) {
                switch (queue.take()) {
                    case Text(var json) -> session.sendMessage(new TextMessage(json));
                    case Close(var status) -> {
                        // the writer itself closes, so it isn't interrupted in the middle of the close frame
                        closeSession(status);
                        return;
                    }
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (IOException | RuntimeException e) {
            log.debug("Sending on {} failed", session.getId(), e);
            close(CloseStatus.SESSION_NOT_RELIABLE);
        }
    }
}
