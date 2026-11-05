package xyz.zlatanov.frakkintoasters.journal.fact;

import xyz.zlatanov.frakkintoasters.state.deck.DeckId;

import java.util.List;

/**
 * Facts about whole decks.
 */
public sealed interface DeckFact extends Fact {

    /**
     * The draw pile and the discard pile were combined and shuffled. Applying it sets the draw pile to
     * {@code drawOrder} (first = top) and clears the discard pile. Cards are listed in full; hiding them is the
     * describers' job.
     */
    record DeckShuffled(DeckId deck, List<?> drawOrder) implements DeckFact {
        public DeckShuffled {
            drawOrder = List.copyOf(drawOrder);
        }
    }
}
