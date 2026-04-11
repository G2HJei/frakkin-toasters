package xyz.zlatanov.frakkintoasters.fake;

import lombok.NoArgsConstructor;
import xyz.zlatanov.frakkintoasters.state.deck.Deck;

@NoArgsConstructor
public class FakeDeck<T> extends Deck<T> {

    private T nextCard = null;

    public FakeDeck(Deck<T> delegate) {
        add(delegate.cards());
    }

    public void nextCard(T card) {
        nextCard = card;
    }

    @Override
    public T draw() {
        if (nextCard != null && cards.contains(nextCard)) {
            cards.remove(nextCard);
            cards.addFirst(nextCard);
            nextCard = null;
        }
        return super.draw();
    }
}
