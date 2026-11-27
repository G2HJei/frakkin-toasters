package xyz.zlatanov.frakkintoasters.engine.journal.fact;

import xyz.zlatanov.frakkintoasters.state.PlayerPile;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard;
import xyz.zlatanov.frakkintoasters.state.card.MotiveCard;
import xyz.zlatanov.frakkintoasters.state.character.Character;
import xyz.zlatanov.frakkintoasters.state.character.CharacterAbility;
import xyz.zlatanov.frakkintoasters.state.deck.DeckId;

import java.util.List;

public sealed interface PlayerFact extends Fact {

    record CharacterSelected(int playerNumber, Character character) implements PlayerFact {
    }

    record CharacterMoved(Character character, Location to) implements PlayerFact {
    }

    record CharacterRemovedFromBoards(Character character) implements PlayerFact {
    }

    record CharacterRemoved(int playerNumber, Character character) implements PlayerFact {
    }

    record CardsGained(int playerNumber, PlayerPile pile, List<?> cards) implements PlayerFact {
        public CardsGained {
            cards = List.copyOf(cards);
        }
    }

    record CardsLost(int playerNumber, PlayerPile pile, List<?> cards) implements PlayerFact {
        public CardsLost {
            cards = List.copyOf(cards);
        }
    }

    record MiracleTokenGained(int playerNumber) implements PlayerFact {
    }

    record MiracleTokenLost(int playerNumber) implements PlayerFact {
    }

    // The player used an ability of his character sheet: the turn keeps it, for the "once per turn" ones.
    record AbilityUsed(int playerNumber, CharacterAbility ability) implements PlayerFact {
    }

    // Something happened that the player's ability answers right after the event, e.g. population lost for "Memento".
    // The turn keeps it until the ability is offered.
    record AbilityTriggered(int playerNumber, CharacterAbility ability) implements PlayerFact {
    }

    record AbilityTriggerCleared(int playerNumber, CharacterAbility ability) implements PlayerFact {
    }

    record LoyaltyCardRevealed(int playerNumber, LoyaltyCard card) implements PlayerFact {
    }

    record MotiveCardRevealed(int playerNumber, MotiveCard card) implements PlayerFact {
    }

    record InfiltrationStarted(int playerNumber) implements PlayerFact {
    }

    record InfiltrationEnded(int playerNumber) implements PlayerFact {
    }

    record CivilianShipRevealed(int playerNumber, int shipId) implements PlayerFact {
    }

    // The ship went back into the deck, where it gets a new id.
    record CivilianShipForgotten(int playerNumber, int shipId) implements PlayerFact {
    }

    record OwnTurnStarted(int playerNumber) implements PlayerFact {
    }

    record LoyaltyCardSeen(int playerNumber, int targetPlayerNumber, LoyaltyCard card) implements PlayerFact {
    }

    // Another player looked at a Final Five card, so its holder reveals it to everyone (rule 5.4.1.1). He keeps it, and
    // shuffles it back among his Loyalty Cards: it changes nothing, it tells everyone what he holds.
    record FinalFiveCardShown(int playerNumber, LoyaltyCard card) implements PlayerFact {
    }

    // Cards the player looked at without taking them, e.g. the Admiral's two Destination Cards: only he knows them.
    // It changes nothing, it tells who learned what.
    record CardsSeen(int playerNumber, DeckId deck, List<?> cards) implements PlayerFact {
        public CardsSeen {
            cards = List.copyOf(cards);
        }
    }
}
