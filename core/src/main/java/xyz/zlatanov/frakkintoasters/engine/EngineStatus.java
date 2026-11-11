package xyz.zlatanov.frakkintoasters.engine;

import xyz.zlatanov.frakkintoasters.state.Team;

import java.util.Optional;

public sealed interface EngineStatus {

    /**
     * @param input what the game waits for, empty while it waits for another player's secret input
     */
    record Waiting(Optional<PendingInput> input) implements EngineStatus {
    }

    record Finished(Team winner) implements EngineStatus {
    }

    static EngineStatus waiting() {
        return new Waiting(Optional.empty());
    }

    static EngineStatus waiting(PendingInput input) {
        return new Waiting(Optional.of(input));
    }

    static EngineStatus finished(Team winner) {
        return new Finished(winner);
    }
}
