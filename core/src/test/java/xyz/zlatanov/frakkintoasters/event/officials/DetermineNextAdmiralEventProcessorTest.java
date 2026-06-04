package xyz.zlatanov.frakkintoasters.event.officials;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.event.EventTestHarness;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static xyz.zlatanov.frakkintoasters.state.board.Location.BRIG;
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
    void shouldElectNoOneWhenAllAreInBrig() {
        moveTo(BRIG, HELENA_CAIN);
        moveTo(BRIG, WILLIAM_ADAMA);

        execute(new DetermineNextAdmiralEvent());

        assertNull(admiral());
    }

}