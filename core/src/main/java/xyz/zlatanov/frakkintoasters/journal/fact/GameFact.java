package xyz.zlatanov.frakkintoasters.journal.fact;

import xyz.zlatanov.frakkintoasters.state.GameStep;
import xyz.zlatanov.frakkintoasters.state.card.ObjectiveCard;

public sealed interface GameFact extends Fact {

    record GameStarted(int playerCount, ObjectiveCard objective) implements GameFact {
    }

    record StepChanged(GameStep step) implements GameFact {
    }

    record TurnStarted(int turn) implements GameFact {
    }

    record CurrentPlayerChanged(int playerNumber) implements GameFact {
    }

    record ActingPlayerChanged(Integer playerNumber) implements GameFact {
    }
}
