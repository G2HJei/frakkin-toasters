package xyz.zlatanov.frakkintoasters.event.loyalty;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;

class GiveLoyaltyCardsEventProcessorTest extends EventTestHarness<GiveLoyaltyCardsEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(KARA_STARBUCK_THRACE);
        player(2).character(GAIUS_BALTAR);
        player(3).character(SAUL_TIGH);
        // player 1 revealed as a Cylon and has another card left
        player(1).loyaltyCards().addOnTop(CYLON_DAMAGE_GALACTICA, NOT_CYLON, MUTINEER).reveal(CYLON_DAMAGE_GALACTICA);
    }

    // "he gives all his remaining facedown Loyalty Cards to a human player of his choice". The Mutineer card is passed
    // on its own.
    @Test
    void shouldGiveTheFacedownCardsToTheHuman() {
        execute(new GiveLoyaltyCardsEvent(1, 3));

        assertEquals(List.of(NOT_CYLON), player(3).loyaltyCards().cards());
        assertEquals(List.of(MUTINEER), player(1).loyaltyCards().cards());
    }

    @Test
    void shouldNotGiveToARevealedCylon() {
        player(3).loyaltyCards().addOnTop(CYLON_SEND_TO_BRIG).reveal(CYLON_SEND_TO_BRIG);
        assertInvalid(new GiveLoyaltyCardsEvent(1, 3));
    }

    @Test
    void shouldNotGiveToHimself() {
        assertInvalid(new GiveLoyaltyCardsEvent(1, 1));
    }

    @Test
    void shouldNotGiveToSomeoneWhoIsNotPlaying() {
        assertInvalid(new GiveLoyaltyCardsEvent(1, 4));
    }
}
