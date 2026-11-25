package xyz.zlatanov.frakkintoasters.backend.lobby;

import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import xyz.zlatanov.frakkintoasters.backend.game.GameBundle.GameStatus;
import xyz.zlatanov.frakkintoasters.backend.game.ProjectionSink;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Who is there. In a lobby a user is online while his room page polls; once the game runs, while he has a connection to
 * it, which the client keeps alive with pings. Kept in memory: after a restart everyone is away until he is back.
 */
@Component
public class Presence {

    static final Duration POLLED = Duration.ofSeconds(10);

    private final ObjectProvider<ProjectionSink>         sinks;
    private final Clock                                  clock;
    private final Map<UUID, Map<UUID, Instant>>          polls = new ConcurrentHashMap<>();

    @Autowired
    public Presence(ObjectProvider<ProjectionSink> sinks) {
        this(sinks, Clock.systemUTC());
    }

    Presence(ObjectProvider<ProjectionSink> sinks, Clock clock) {
        this.sinks = sinks;
        this.clock = clock;
    }

    /**
     * The user's room page asked for the room.
     */
    public void polled(UUID gameBundleId, UUID userId) {
        polls.computeIfAbsent(gameBundleId, ignored -> new ConcurrentHashMap<>()).put(userId, clock.instant());
    }

    public Set<UUID> online(UUID gameBundleId, GameStatus status) {
        if (status != GameStatus.LOBBY) {
            // a lobby that started forgets its polls
            polls.remove(gameBundleId);
            return sinks.getIfAvailable(() -> ProjectionSink.NONE).connected(gameBundleId);
        }
        val since = clock.instant().minus(POLLED);
        return polls.getOrDefault(gameBundleId, Map.of()).entrySet().stream()
                .filter(poll -> poll.getValue().isAfter(since))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }
}
