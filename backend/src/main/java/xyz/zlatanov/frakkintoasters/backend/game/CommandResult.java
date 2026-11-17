package xyz.zlatanov.frakkintoasters.backend.game;

import xyz.zlatanov.frakkintoasters.backend.game.ProjectionSink.GameMessage;

/**
 * What a command from a connection came to. A rejection goes only to the sender.
 */
public sealed interface CommandResult {

    record Accepted() implements CommandResult {
    }

    /**
     * @param game the current game for the sender, when his command named an old state
     */
    record Rejected(Reason reason, String message, GameMessage game) implements CommandResult {
    }

    enum Reason {
        /**
         * The command answers a state the game has moved on from.
         */
        STALE,
        /**
         * The engine refused the input, or it couldn't be read. Nothing changed.
         */
        INVALID,
        /**
         * A bug or a failed write. Nothing changed.
         */
        FAILURE
    }

    static CommandResult accepted() {
        return new Accepted();
    }

    static CommandResult rejected(Reason reason, String message) {
        return new Rejected(reason, message, null);
    }
}
