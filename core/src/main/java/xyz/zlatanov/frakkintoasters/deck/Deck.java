package xyz.zlatanov.frakkintoasters.deck;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Deck<T> {

    private final List<T> cards          = new ArrayList<>();
    private final List<T> discardedCards = new ArrayList<>();

    public void add(T card) {
        add(List.of(card));
    }

    public void add(List<T> toAdd) {
        cards.addAll(toAdd);
    }

    public T draw() {
        return cards.removeFirst();
    }

    public int size() {
        return cards.size();
    }

    public int discardSize() {
        return discardedCards.size();
    }

    public void discard(T card) {
        discardedCards.add(card);
    }

    public void shuffle() {
        cards.addAll(discardedCards);
        discardedCards.clear();
        Collections.shuffle(cards);
    }
}
