package xyz.zlatanov.frakkintoasters.event.loyalty;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.KARA_STARBUCK_THRACE;

class RevealCylonOnExecutionEventProcessorTest extends EventTestHarness<RevealCylonOnExecutionEvent> {

    @BeforeEach
    void setUp() {
        player(2).character(KARA_STARBUCK_THRACE);
        player(2).loyaltyCards().addOnTop(CYLON_DAMAGE_GALACTICA, CYLON_REDUCE_MORALE);
    }

    // The chosen card is revealed without its power, the other goes to a human with the rest.
    @Test
    void shouldRevealTheChosenCardWithoutItsPower() {
        execute(new RevealCylonOnExecutionEvent(2, CYLON_REDUCE_MORALE));

        assertEquals(List.of(CYLON_REDUCE_MORALE), player(2).loyaltyCards().revealedCards());
        assertEquals(List.of(CYLON_DAMAGE_GALACTICA), player(2).loyaltyCards().cards());
        assertFollowup(new FinishCylonRevealEvent(2, false));
    }

    @Test
    void shouldRejectACardHeDoesntHold() {
        assertInvalid(new RevealCylonOnExecutionEvent(2, CYLON_SEND_TO_BRIG));
    }
}
