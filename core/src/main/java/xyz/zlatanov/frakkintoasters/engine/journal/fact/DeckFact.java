package xyz.zlatanov.frakkintoasters.engine.journal.fact;

import xyz.zlatanov.frakkintoasters.state.deck.DeckId;

import java.util.List;

public sealed interface DeckFact extends Fact {

    record DeckShuffled(DeckId deck, List<?> drawOrder) implements DeckFact {
        public DeckShuffled {
            drawOrder = List.copyOf(drawOrder);
        }
    }

    record CardsDrawn(DeckId deck, List<?> cards) implements DeckFact {
        public CardsDrawn {
            cards = List.copyOf(cards);
        }
    }

    record CardsAddedOnTop(DeckId deck, List<?> cards) implements DeckFact {
        public CardsAddedOnTop {
            cards = List.copyOf(cards);
        }
    }

    record CardsAddedToBottom(DeckId deck, List<?> cards) implements DeckFact {
        public CardsAddedToBottom {
            cards = List.copyOf(cards);
        }
    }

    // A sandbox setup takes the cards out of the draw pile, to stack them or give them to a player, so no copy appears.
    record CardsPulled(DeckId deck, List<?> cards) implements DeckFact {
        public CardsPulled {
            cards = List.copyOf(cards);
        }
    }

    record CardsDiscarded(DeckId deck, List<?> cards) implements DeckFact {
        public CardsDiscarded {
            cards = List.copyOf(cards);
        }
    }

    // Cards of the deck that leave the game, e.g. a "Food Rationing" that succeeded. They were in no pile.
    record CardsRemovedFromGame(DeckId deck, List<?> cards) implements DeckFact {
        public CardsRemovedFromGame {
            cards = List.copyOf(cards);
        }
    }
}
