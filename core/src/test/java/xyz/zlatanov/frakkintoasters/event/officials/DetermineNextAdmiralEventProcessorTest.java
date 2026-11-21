package xyz.zlatanov.frakkintoasters.event.officials;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.board.Location.BRIG;
import static xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard.CYLON_REDUCE_MORALE;
import static xyz.zlatanov.frakkintoasters.state.character.Character.HELENA_CAIN;
import static xyz.zlatanov.frakkintoasters.state.character.Character.WILLIAM_ADAMA;

class DetermineNextAdmiralEventProcessorTest extends EventTestHarness<DetermineNextAdmiralEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(WILLIAM_ADAMA);
        player(2).character(HELENA_CAIN);
    }

    @Test
    void shouldRespectLineOfSuccession() {
        execute(new DetermineNextAdmiralEvent());
        assertEquals(HELENA_CAIN, admiral());
    }

    @Test
    void shouldIgnoreExcludedPlayer() {
        execute(new DetermineNextAdmiralEvent(2));
        assertEquals(WILLIAM_ADAMA, admiral());
    }

    @Test
    void shouldStartWith2Nukes() {
        execute(new DetermineNextAdmiralEvent());
        assertEquals(2, game.nukes());
    }

    @Test
    void shouldIgnoreCharactersInBrig() {
        moveTo(BRIG, HELENA_CAIN);
        execute(new DetermineNextAdmiralEvent());
        assertEquals(WILLIAM_ADAMA, admiral());
    }

    @Test
    void shouldElectTheHighestInTheBrigWhenAllAreThere() {
        moveTo(BRIG, HELENA_CAIN);
        moveTo(BRIG, WILLIAM_ADAMA);

        execute(new DetermineNextAdmiralEvent());

        assertEquals(HELENA_CAIN, admiral()); // until one of them leaves the "Brig" (rule 4.4.2.3)
    }

    @Test
    void shouldSkipARevealedCylon() {
        player(2).loyaltyCards().addOnTop(CYLON_REDUCE_MORALE).reveal(CYLON_REDUCE_MORALE);

        execute(new DetermineNextAdmiralEvent());

        assertEquals(WILLIAM_ADAMA, admiral());
    }

}