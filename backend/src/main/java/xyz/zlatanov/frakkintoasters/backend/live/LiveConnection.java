package xyz.zlatanov.frakkintoasters.backend.live;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import xyz.zlatanov.frakkintoasters.projection.Viewer;

import java.io.IOException;
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
    private final BlockingQueue<String> queue = new LinkedBlockingQueue<>(MAX_QUEUED);
    private final Thread                writer;

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

    /**
     * Queues a message. Never blocks: a connection whose queue is full is closed.
     */
    public void send(String json) {
        if (!queue.offer(json)) {
            log.warn("Connection {} fell behind, closing it", session.getId());
            close(CloseStatus.SESSION_NOT_RELIABLE.withReason("Fell behind"));
        }
    }

    public void close(CloseStatus status) {
        writer.interrupt();
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
                var message = queue.take();
                session.sendMessage(new TextMessage(message));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (IOException | RuntimeException e) {
            log.debug("Sending on {} failed", session.getId(), e);
            close(CloseStatus.SESSION_NOT_RELIABLE);
        }
    }
}
