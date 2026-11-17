package xyz.zlatanov.frakkintoasters.engine.journal.fact;

import xyz.zlatanov.frakkintoasters.state.PlayerPile;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard;
import xyz.zlatanov.frakkintoasters.state.card.MotiveCard;
import xyz.zlatanov.frakkintoasters.state.character.Character;
import xyz.zlatanov.frakkintoasters.state.deck.DeckId;

import java.util.List;

/**
 * Facts about players and their characters.
 */
public sealed interface PlayerFact extends Fact {

    record CharacterSelected(int playerNumber, Character character) implements PlayerFact {
    }

    record CharacterMoved(Character character, Location to) implements PlayerFact {
    }

    record CharacterRemovedFromBoards(Character character) implements PlayerFact {
    }

    // The character leaves the game, e.g. when executed: the player has none until he chooses a new one.
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

    // A random Loyalty Card of the target that the player looked at: only he knows it.
    record LoyaltyCardSeen(int playerNumber, int targetPlayerNumber, LoyaltyCard card) implements PlayerFact {
    }

    // Cards the player looked at without taking them, e.g. the Admiral's two Destination Cards: only he knows them.
    // It changes nothing, it tells who learned what.
    record CardsSeen(int playerNumber, DeckId deck, List<?> cards) implements PlayerFact {
        public CardsSeen {
            cards = List.copyOf(cards);
        }
    }
}
