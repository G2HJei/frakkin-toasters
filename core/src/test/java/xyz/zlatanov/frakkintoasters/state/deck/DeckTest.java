package xyz.zlatanov.frakkintoasters.state.deck;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DeckTest {
    Deck<Card> deck = new Deck<>();
    Card       card = new Card();

    @BeforeEach
    void setUp() {
        deck.add(List.of(new Card()));
    }

    @Test
    void shouldAddCardToBottomOfDeck() {
        deck.add(card);
        assertEquals(2, deck.size());
    }

    @Test
    void shouldDrawCardFromTopOfDeck() {
        deck.add(card);
        assertNotSame(card, deck.draw());
    }

    @Test
    void shouldDiscardCard() {
        deck.discard(card);
        assertEquals(1, deck.discardSize());
    }

    @Test
    void shouldShuffleDeck() {
        deck.discard(card);
        deck.shuffle();
        assertEquals(2, deck.size());
        assertEquals(0, deck.discardSize());
    }

    @Test
    void shouldShowOnlyLastDiscarded() {
        val lastCard = new Card();
        deck.discard(new Card())
                .discard(lastCard);
        assertEquals(lastCard, deck.lastDiscarded());
    }

    @Test
    void shouldRevealCard() {
        deck.add(card);
        deck.reveal(card);
        assertTrue(deck.revealedCards().contains(card));
    }

    static class Card {

    }
}