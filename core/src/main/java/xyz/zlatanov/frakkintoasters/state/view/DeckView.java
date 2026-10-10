package xyz.zlatanov.frakkintoasters.state.view;

import java.util.List;

public interface DeckView<T> {

    List<T> cards();

    List<T> discardedCards();

    int size();

    int discardSize();

    T lastDiscarded();

    List<T> revealedCards();

    boolean isEmpty();
}
