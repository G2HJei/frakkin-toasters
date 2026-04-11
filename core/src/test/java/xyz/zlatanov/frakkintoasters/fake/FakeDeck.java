package xyz.zlatanov.frakkintoasters.fake;

import xyz.zlatanov.frakkintoasters.state.deck.Deck;

public class FakeDeck<T> extends Deck<T> {

    public T nextCard = null;

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
