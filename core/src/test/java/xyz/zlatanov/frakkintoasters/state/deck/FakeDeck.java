package xyz.zlatanov.frakkintoasters.state.deck;

public class FakeDeck<T> extends Deck<T> {

    T nextCard = null;

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
