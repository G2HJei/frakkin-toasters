package xyz.zlatanov.frakkintoasters.engine.journal.fact;

import xyz.zlatanov.frakkintoasters.state.GameStep;
import xyz.zlatanov.frakkintoasters.state.Team;
import xyz.zlatanov.frakkintoasters.state.card.ObjectiveCard;
import xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardType;

import java.util.List;

public sealed interface GameFact extends Fact {

    record GameStarted(int playerCount, ObjectiveCard objective) implements GameFact {
    }

    record DieRollStarted() implements GameFact {
    }

    // A reroll is another DieRolled: it replaces the face and keeps the cards played on the roll.
    record DieRolled(int value) implements GameFact {
    }

    record DieRollCardPlayed(SkillCardType card, int modifier) implements GameFact {
    }

    record StepChanged(GameStep step) implements GameFact {
    }

    record TurnStarted(int turn) implements GameFact {
    }

    record TurnEndedEarly() implements GameFact {
    }

    record CurrentPlayerChanged(int playerNumber) implements GameFact {
    }

    record ActingPlayerChanged(Integer playerNumber) implements GameFact {
    }

    record CrisisCardPlayed(CrisisCard card) implements GameFact {
    }

    record CrisisCardsCleared(List<CrisisCard> cards) implements GameFact {
        public CrisisCardsCleared {
            cards = List.copyOf(cards);
        }
    }

    record GameEnded(Team winner) implements GameFact {
    }
}
