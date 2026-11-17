package xyz.zlatanov.frakkintoasters.backend.live;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Component;
import xyz.zlatanov.frakkintoasters.backend.game.ProjectionSink;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The live connections of each game. It queues what each connection's viewer gets after a command; each connection
 * sends from its own queue.
 */
@Component
@RequiredArgsConstructor
public class ConnectionPool implements ProjectionSink {

    private final Messages                                 messages;
    private final ConcurrentHashMap<UUID, Set<LiveConnection>> games = new ConcurrentHashMap<>();

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
