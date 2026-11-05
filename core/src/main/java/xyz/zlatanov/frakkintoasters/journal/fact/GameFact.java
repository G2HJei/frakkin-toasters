package xyz.zlatanov.frakkintoasters.journal.fact;

import xyz.zlatanov.frakkintoasters.state.GameStep;

public sealed interface GameFact extends Fact {

    record StepChanged(GameStep step) implements GameFact {
    }

    record TurnStarted(int turn) implements GameFact {
    }

    record CurrentPlayerChanged(int playerNumber) implements GameFact {
    }

    record ActingPlayerChanged(Integer playerNumber) implements GameFact {
    }
}
