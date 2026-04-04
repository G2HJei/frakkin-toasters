package xyz.zlatanov.frakkintoasters.state.deck;

import lombok.val;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Deck<T> {

    protected final List<T> cards          = new ArrayList<>();
    private final   List<T> revealedCards  = new ArrayList<>();
    private final   List<T> discardedCards = new ArrayList<>();

    public Deck<T> add(T card) {
        add(List.of(card));
        return this;
    }

    public Deck<T> add(List<T> toAdd) {
        cards.addAll(toAdd);
        return this;
    }

    public Deck<T> remove(T card) {
        assert cards.contains(card);
        cards.remove(card);
        return this;
    }

    public T draw() {
        return cards.removeFirst();
    }

    public List<T> draw(int cards) {
        val result = new ArrayList<T>();
        while (cards-- > 0) {
            result.add(draw());
        }
        return result;
    }

    public List<T> cards() {
        return Collections.unmodifiableList(cards);
    }

    public int size() {
        return cards.size();
    }

    public Deck<T> discard(T card) {
        discardedCards.add(card);
        return this;
    }

    public int discardSize() {
        return discardedCards.size();
    }

    public T lastDiscarded() {
        return discardedCards.isEmpty() ? null : discardedCards.getLast();
    }

    public Deck<T> reveal(T card) {
        assert cards.contains(card);
        cards.remove(card);
        revealedCards.add(card);
        return this;
    }

    public List<T> revealedCards() {
        return Collections.unmodifiableList(revealedCards);
    }

    public Deck<T> shuffle() {
        cards.addAll(discardedCards);
        discardedCards.clear();
        Collections.shuffle(cards);
        return this;
    }
}
