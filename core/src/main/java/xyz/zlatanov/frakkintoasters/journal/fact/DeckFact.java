package xyz.zlatanov.frakkintoasters.journal.fact;

import xyz.zlatanov.frakkintoasters.state.deck.DeckId;

import java.util.List;

/**
 * Facts about the shared decks.
 */
public sealed interface DeckFact extends Fact {

    /**
     * The draw pile and the discard pile were combined and shuffled. Applying it sets the draw pile to
     * {@code drawOrder} and clears the discard pile.
     */
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

    record CardsDiscarded(DeckId deck, List<?> cards) implements DeckFact {
        public CardsDiscarded {
            cards = List.copyOf(cards);
        }
    }
}
