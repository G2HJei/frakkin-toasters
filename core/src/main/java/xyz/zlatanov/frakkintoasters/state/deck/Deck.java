package xyz.zlatanov.frakkintoasters.state.deck;

import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.val;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
@Accessors(fluent = true)
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

    public List<T> draw(int cards) {
        val result = new ArrayList<T>();
        while (cards-- > 0) {
            result.add(draw());
        }
        return result;
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
