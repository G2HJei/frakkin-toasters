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
        selectCharacter(1, WILLIAM_ADAMA);
        selectCharacter(2, HELENA_CAIN);

        executeAndAssertNoFollowup(new DetermineNextAdmiralEvent());
    }

    @Test
    void shouldRespectLineOfSuccession() {
        assertEquals(HELENA_CAIN, admiral());
    }

    @Test
    void shouldStartWith2Nukes() {
        assertEquals(2, game.nukes());
    }

    @Test
    void shouldIgnoreCharactersInBrig() {
        moveTo(BRIG, HELENA_CAIN);
        executeAndAssertNoFollowup(new DetermineNextAdmiralEvent());
        assertEquals(WILLIAM_ADAMA, admiral());
    }

    @Test
    void shouldElectNoOneWhenAllAreInBrig() {
        moveTo(BRIG, HELENA_CAIN);
        moveTo(BRIG, WILLIAM_ADAMA);
        executeAndAssertNoFollowup(new DetermineNextAdmiralEvent());
        assertNull(admiral());
    }

}