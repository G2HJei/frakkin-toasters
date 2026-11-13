package xyz.zlatanov.frakkintoasters.state.deck;

import xyz.zlatanov.frakkintoasters.state.Mutates;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.val;
import xyz.zlatanov.frakkintoasters.state.view.DeckView;

import java.util.*;

@RequiredArgsConstructor
@EqualsAndHashCode
public class Deck<T> implements DeckView<T> {

    private final   boolean autoShuffle;
    protected final List<T> cards          = new ArrayList<>(); //todo use Deque?
    private final   List<T> revealedCards  = new ArrayList<>();
    private final   List<T> discardedCards = new ArrayList<>();

    public Deck() {
        this(true);
    }

    @Mutates
    public Deck<T> addOnTop(T card) {
        addOnTop(List.of(card));
        return this;
    }

    @SafeVarargs
    @Mutates
    public final Deck<T> addOnTop(T... toAdd) {
        return addOnTop(Arrays.stream(toAdd).toList());
    }

    @Mutates
    public Deck<T> addOnTop(List<T> toAdd) {
        toAdd.reversed().forEach(cards::addFirst);
        return this;
    }

    @Mutates
    public Deck<T> addToBottom(T card) {
        cards.addLast(card);
        return this;
    }

    @Mutates
    public Deck<T> remove(T cardToRemove) {
        return remove(List.of(cardToRemove));
    }

    @SafeVarargs
    @Mutates
    public final Deck<T> remove(T... cardsToRemove) {
        return remove(List.of(cardsToRemove));
    }

    @Mutates
    public Deck<T> remove(List<T> cardsToRemove) {
        assert new HashSet<>(cards).containsAll(cardsToRemove);
        cardsToRemove.forEach(cards::remove);
        return this;
    }

    @Mutates
    public T draw() {
        // in practice, only civilian ships deck can be empty, so it has to be checked before drawing to keep this method return non-optional result
        // never reshuffles: when an emptied deck has to be reshuffled (see autoShuffle()), the calling op does it and logs it
        return cards.removeFirst();
    }

    /**
     * @return whether the discard pile is shuffled back in when a draw empties the draw pile
     */
    public boolean autoShuffle() {
        return autoShuffle;
    }

    /**
     * Removes the top n cards from the draw pile.
     *
     * @return the removed cards (first = top)
     */
    @Mutates
    public List<T> removeTop(int n) {
        val removed = new ArrayList<>(cards.subList(0, n));
        cards.subList(0, n).clear();
        return removed;
    }

    /**
     * @return the top n cards (first = top) without drawing them.
     */
    public List<T> top(int n) {
        return new ArrayList<>(cards.subList(0, n));
    }

    @Mutates
    public List<T> draw(int cards) {
        val result = new ArrayList<T>();
        while (cards-- > 0) {
            result.add(draw());
        }
        return result;
    }

    @Override
    public List<T> cards() {
        return List.copyOf(cards);
    }

    @Override
    public List<T> discardedCards() {
        return List.copyOf(discardedCards);
    }

    @Override
    public int size() {
        return cards.size();
    }

    @Mutates
    public Deck<T> discard(T cardToDiscard) {
        return discard(List.of(cardToDiscard));
    }

    @Mutates
    public Deck<T> discard(List<T> cardsToDiscard) {
        discardedCards.addAll(cardsToDiscard);
        return this;
    }

    @Override
    public int discardSize() {
        return discardedCards.size();
    }

    @Override
    public T lastDiscarded() {
        return discardedCards.isEmpty() ? null : discardedCards.getLast();
    }

    @Mutates
    public Deck<T> reveal(T card) {
        assert cards.contains(card);
        cards.remove(card);
        revealedCards.add(card);
        return this;
    }

    @Override
    public List<T> revealedCards() {
        return List.copyOf(revealedCards);
    }

    @Mutates
    public Deck<T> setDrawOrder(List<? extends T> newOrder) {
        cards.clear();
        cards.addAll(newOrder);
        discardedCards.clear();
        return this;
    }

    @Override
    public boolean isEmpty() {
        return cards.isEmpty();
    }

}
