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
        deck.addOnTop(List.of(new Card()));
    }

    @Test
    void shouldAddOnTopCardToBottomOfDeck() {
        deck.addOnTop(card);
        assertEquals(2, deck.size());
    }

    @Test
    void shouldDrawCardFromTopOfDeck() {
        deck.addToBottom(card);
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
        deck.addOnTop(card);
        deck.reveal(card);
        assertTrue(deck.revealedCards().contains(card));
    }

    @Test
    void shouldAutoShuffleWhenEmptyAfterDraw() {
        val secondCard = new Card();
        deck.discard(secondCard);

        deck.draw();

        assertEquals(List.of(secondCard), deck.cards());
    }

    @Test
    void shouldNotAutoShuffleIfConfigured() {
        val manualShuffleDeck = new Deck<>(false);
        manualShuffleDeck.addOnTop(new Card());
        manualShuffleDeck.discard(new Card());

        manualShuffleDeck.draw();

        assertTrue(manualShuffleDeck.cards.isEmpty());
    }

    static class Card {

    }
}