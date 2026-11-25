package xyz.zlatanov.frakkintoasters.backend.live;

import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import xyz.zlatanov.frakkintoasters.backend.game.ProjectionSink;
import xyz.zlatanov.frakkintoasters.projection.Viewer;

import java.time.Duration;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * The live connections of each game. It queues what each connection's viewer gets after a command; each connection
 * sends from its own queue.
 */
@Component
public class ConnectionPool implements ProjectionSink {

    public static final CloseStatus SEAT_CHANGED = new CloseStatus(4001, "Seat changed");

    private final Messages                                     messages;
    private final Duration                                     silence;
    private final ConcurrentHashMap<UUID, Set<LiveConnection>> games = new ConcurrentHashMap<>();

    /**
     * @param silence how long a connection may stay silent: the client pings every 20 seconds, so one that is quiet
     *                for longer lost its network, and its user shows as away
     */
    public ConnectionPool(Messages messages, @Value("${frakkin.socket-silence:60s}") Duration silence) {
        this.messages = messages;
        this.silence = silence;
    }

    public void add(LiveConnection connection) {
        games.computeIfAbsent(connection.gameBundleId(), ignored -> ConcurrentHashMap.newKeySet()).add(connection);
    }

    public void remove(LiveConnection connection) {
        games.computeIfPresent(connection.gameBundleId(), (ignored, connections) -> {
            connections.remove(connection);
            return connections.isEmpty() ? null : connections;
        });
    }

    public Set<LiveConnection> of(UUID gameBundleId) {
        return Set.copyOf(games.getOrDefault(gameBundleId, Set.of()));
    }

    /**
     * Sends the message to every connection of the game whose viewer may have it, e.g. a chat message to the readers of
     * its channel.
     */
    public void broadcast(UUID gameBundleId, Predicate<Viewer> to, String json) {
        for (val connection : of(gameBundleId)) {
            if (to.test(connection.viewer())) {
                connection.send(json);
            }
        }
    }

    @Override
    public Set<UUID> connected(UUID gameBundleId) {
        return of(gameBundleId).stream().map(LiveConnection::userId).collect(Collectors.toSet());
    }

    // A connection the client stopped pinging: the close tells the handler, which removes it.
    @Scheduled(fixedDelayString = "${frakkin.socket-sweep:15s}")
    void closeSilent() {
        games.values().forEach(connections -> connections.stream()
                .filter(connection -> connection.silence().compareTo(silence) > 0)
                .forEach(connection -> connection.close(CloseStatus.SESSION_NOT_RELIABLE.withReason("Silent"))));
    }

    /**
     * Closes the users' connections to the game with {@link #SEAT_CHANGED}, after which the client reconnects at once:
     * the handshake gives the new connection the viewer of the seat he holds now.
     */
    @Override
    public void reconnect(UUID gameBundleId, Set<UUID> userIds) {
        for (val connection : of(gameBundleId)) {
            if (userIds.contains(connection.userId())) {
                connection.closeAfterSending(SEAT_CHANGED);
            }
        }
    }

    @Override
    public void publish(UUID gameBundleId, Publication publication) {
        // a viewer's frames are built once, and written to JSON once, however many tabs he has open
        val written = new IdentityHashMap<Frames, List<String>>();
        for (val connection : games.getOrDefault(gameBundleId, Set.of())) {
            publication.forViewer(connection.viewer())
                    .map(frames -> written.computeIfAbsent(frames, messages::of))
                    .ifPresent(json -> json.forEach(connection::send));
        }
    }
}
