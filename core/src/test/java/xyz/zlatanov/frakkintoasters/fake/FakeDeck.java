package xyz.zlatanov.frakkintoasters.fake;

import lombok.NoArgsConstructor;
import xyz.zlatanov.frakkintoasters.state.deck.Deck;

import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;

@NoArgsConstructor
public class FakeDeck<T> extends Deck<T> {

    private final Deque<T> nextCards = new LinkedList<>();

    public FakeDeck(Deck<T> delegate) {
        addOnTop(delegate.cards());
    }

    @SafeVarargs
    public final FakeDeck<T> nextCard(T... cards) {
        nextCards.addAll(Arrays.asList(cards));
        return this;
    }

    @Override
    public T draw() {
        if (!nextCards.isEmpty()) {
            T nextCard = nextCards.removeFirst();
            cards.remove(nextCard);
            cards.addFirst(nextCard);
        }
        return super.draw();
    }

    public FakeDeck<T> clear() {
        while (!cards().isEmpty()) {
            draw();
        }
        return this;
    }
}
