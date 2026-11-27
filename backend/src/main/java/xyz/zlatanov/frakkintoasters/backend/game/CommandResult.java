package xyz.zlatanov.frakkintoasters.backend.game;

import xyz.zlatanov.frakkintoasters.backend.game.ProjectionSink.GameMessage;

public sealed interface CommandResult {

    record Accepted() implements CommandResult {
    }

    record Rejected(Reason reason, String message, GameMessage game) implements CommandResult {
    }

    enum Reason {
        STALE,
        INVALID,
        FAILURE
    }

    static CommandResult accepted() {
        return new Accepted();
    }

    static CommandResult rejected(Reason reason, String message) {
        return new Rejected(reason, message, null);
    }
}
