package xyz.zlatanov.frakkintoasters.fake;

import java.util.Deque;
import java.util.LinkedList;
import lombok.NoArgsConstructor;
import xyz.zlatanov.frakkintoasters.state.deck.Deck;

@NoArgsConstructor
public class FakeDeck<T> extends Deck<T> {

    private final Deque<T> nextCards = new LinkedList<>();

    public FakeDeck(Deck<T> delegate) {
        add(delegate.cards());
    }

    public FakeDeck<T> nextCard(T card) {
        nextCards.add(card);
        return this;
    }

    @Override
    public T draw() {
        if (!nextCards.isEmpty()) {
            T nextCard = nextCards.removeFirst();
            if (cards.contains(nextCard)) {
                cards.remove(nextCard);
                cards.addFirst(nextCard);
            }
        }
        return super.draw();
    }
}
