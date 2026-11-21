package xyz.zlatanov.frakkintoasters.state.view;

import java.util.List;

public interface DeckView<T> {

    List<T> cards();

    /**
     * @return the bottom card of the draw pile, e.g. for "Consult the Oracle"
     */
    default T bottom() {
        return cards().getLast();
    }

    List<T> discardedCards();

    int size();

    int discardSize();

    T lastDiscarded();

    List<T> revealedCards();

    boolean isEmpty();
}
