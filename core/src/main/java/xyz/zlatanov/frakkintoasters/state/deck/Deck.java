package xyz.zlatanov.frakkintoasters.state.deck;

import lombok.val;

import java.util.*;

public class Deck<T> {

    protected final List<T> cards          = new ArrayList<>();
    private final   List<T> revealedCards  = new ArrayList<>();
    private final   List<T> discardedCards = new ArrayList<>();

    public Deck<T> add(T card) {
        add(List.of(card));
        return this;
    }

    @SafeVarargs
    public final Deck<T> add(T... toAdd) {
        return add(Arrays.stream(toAdd).toList());
    }

    public Deck<T> add(List<T> toAdd) {
        cards.addAll(toAdd);
        return this;
    }

    public Deck<T> addToBottom(T card) {
        cards.addLast(card);
        return this;
    }

    public Deck<T> remove(T cardToRemove) {
        return remove(List.of(cardToRemove));
    }

    @SafeVarargs
    public final Deck<T> remove(T... cardsToRemove) {
        return remove(List.of(cardsToRemove));
    }

    public Deck<T> remove(List<T> cardsToRemove) {
        assert new HashSet<>(cards).containsAll(cardsToRemove);
        cards.removeAll(cardsToRemove);
        return this;
    }

    public T draw() {//todo what if empty??
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

    public List<T> discardedCards() {
        return Collections.unmodifiableList(discardedCards);
    }

    public int size() {
        return cards.size();
    }

    public Deck<T> discard(T cardToDiscard) {
        return discard(List.of(cardToDiscard));
    }

    public Deck<T> discard(List<T> cardsToDiscard) {
        discardedCards.addAll(cardsToDiscard);
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

    public boolean isEmpty() {
        return cards.isEmpty();
    }
}
