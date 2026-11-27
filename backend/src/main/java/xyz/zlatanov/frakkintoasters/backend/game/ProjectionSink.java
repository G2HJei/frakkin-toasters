package xyz.zlatanov.frakkintoasters.backend.game;

import xyz.zlatanov.frakkintoasters.projection.GameProjection;
import xyz.zlatanov.frakkintoasters.projection.GameReport;
import xyz.zlatanov.frakkintoasters.projection.LiveUpdateProjection;
import xyz.zlatanov.frakkintoasters.projection.Viewer;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface ProjectionSink {

    /**
     * Called under the game's lock, after the command's entries were stored, so the connections get the commands in the
     * order they ran. The sink only queues what it gets, it must not send under the lock.
     */
    void publish(UUID gameBundleId, Publication publication);

    default void reconnect(UUID gameBundleId, Set<UUID> userIds) {
    }

    default Set<UUID> connected(UUID gameBundleId) {
        return Set.of();
    }

    interface Publication {

        Optional<Frames> forViewer(Viewer viewer);

        default boolean replacesLog() {
            return false;
        }
    }

    record Frames(LiveUpdateProjection update, LiveUpdateProjection log, GameMessage game, GameReport report) {
    }

    record GameMessage(String token, GameProjection game) {
    }

    ProjectionSink NONE = (gameBundleId, publication) -> {
    };
}
