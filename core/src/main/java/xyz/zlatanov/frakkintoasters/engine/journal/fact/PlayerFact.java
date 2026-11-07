package xyz.zlatanov.frakkintoasters.engine.journal.fact;

import xyz.zlatanov.frakkintoasters.state.PlayerPile;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.character.Character;

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

    record InfiltrationStarted(int playerNumber) implements PlayerFact {
    }

    record CivilianShipRevealed(int playerNumber, int shipId) implements PlayerFact {
    }
}
