package xyz.zlatanov.frakkintoasters.backend.game;

import xyz.zlatanov.frakkintoasters.projection.GameProjection;
import xyz.zlatanov.frakkintoasters.projection.GameReport;
import xyz.zlatanov.frakkintoasters.projection.LiveUpdateProjection;
import xyz.zlatanov.frakkintoasters.projection.Viewer;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Where the projections of a command go: the live connections of the game.
 */
public interface ProjectionSink {

    /**
     * Called under the game's lock, after the command's entries were stored, so the connections get the commands in the
     * order they ran. The sink only queues what it gets, it must not send under the lock.
     */
    void publish(UUID gameBundleId, Publication publication);

    /**
     * The users' seats changed: their connections to the game reconnect, with the viewer of their new seat.
     */
    default void reconnect(UUID gameBundleId, Set<UUID> userIds) {
    }

    /**
     * @return the users with an open connection to the game
     */
    default Set<UUID> connected(UUID gameBundleId) {
        return Set.of();
    }

    /**
     * What each viewer gets after a command, built once per viewer.
     */
    interface Publication {

        /**
         * @return the frames for the viewer, empty when nothing he may see changed
         */
        Optional<Frames> forViewer(Viewer viewer);

        /**
         * @return whether the publication replaces what the viewers know, e.g. after an undo: the log as a whole
         */
        default boolean replacesLog() {
            return false;
        }
    }

    /**
     * @param update what happened since the last command, {@code null} when the viewer may see none of it
     * @param log    the whole log, instead of an update, when the publication replaces it
     * @param report the report of the ended game: on a new connection, and with the command that ended it
     */
    record Frames(LiveUpdateProjection update, LiveUpdateProjection log, GameMessage game, GameReport report) {
    }

    /**
     * The game as a viewer may see it, with the token that names its state.
     */
    record GameMessage(String token, GameProjection game) {
    }

    ProjectionSink NONE = (gameBundleId, publication) -> {
    };
}
