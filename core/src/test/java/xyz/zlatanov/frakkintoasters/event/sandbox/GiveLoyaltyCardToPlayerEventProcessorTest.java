package xyz.zlatanov.frakkintoasters.event.sandbox;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard.*;

class GiveLoyaltyCardToPlayerEventProcessorTest extends EventTestHarness<GiveLoyaltyCardToPlayerEvent> {

    @Test
    void theCardComesOutOfTheLoyaltyDeck() {
        loyaltyDeck.clear();
        loyaltyDeck.addOnTop(NOT_CYLON_SELFISH);

        execute(new GiveLoyaltyCardToPlayerEvent(1, NOT_CYLON_SELFISH));

        assertEquals(List.of(NOT_CYLON_SELFISH), player(1).loyaltyCards().cards());
        assertFalse(loyaltyDeck.cards().contains(NOT_CYLON_SELFISH));
    }

    @Test
    void orOutOfTheNotACylonDeck() {
        loyaltyDeck.clear();
        loyaltyNotCylonDeck.clear();
        loyaltyNotCylonDeck.addOnTop(FINAL_FIVE_YOU_ARE_EXECUTED);

        execute(new GiveLoyaltyCardToPlayerEvent(2, FINAL_FIVE_YOU_ARE_EXECUTED));

        assertEquals(List.of(FINAL_FIVE_YOU_ARE_EXECUTED), player(2).loyaltyCards().cards());
        assertFalse(loyaltyNotCylonDeck.cards().contains(FINAL_FIVE_YOU_ARE_EXECUTED));
    }

    // The "You Are a Cylon" cards aren't a deck until they are dealt into the Loyalty deck.
    @Test
    void aCylonCardNobodyHas() {
        loyaltyDeck.clear();

        execute(new GiveLoyaltyCardToPlayerEvent(3, CYLON_REDUCE_MORALE));

        assertEquals(List.of(CYLON_REDUCE_MORALE), player(3).loyaltyCards().cards());
    }

    @Test
    void notACylonCardSomeoneHas() {
        loyaltyDeck.clear();
        player(1).loyaltyCards().addOnTop(CYLON_REDUCE_MORALE);

        assertInvalid(new GiveLoyaltyCardToPlayerEvent(3, CYLON_REDUCE_MORALE));
    }

    @Test
    void notACardThatIsInNoDeck() {
        loyaltyDeck.clear();
        loyaltyNotCylonDeck.clear();

        assertInvalid(new GiveLoyaltyCardToPlayerEvent(1, NOT_CYLON_SELFISH));
    }

    @Test
    void notWithoutACard() {
        assertInvalid(new GiveLoyaltyCardToPlayerEvent(1, null));
    }
}
