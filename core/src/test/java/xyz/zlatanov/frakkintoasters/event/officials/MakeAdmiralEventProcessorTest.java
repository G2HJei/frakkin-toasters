package xyz.zlatanov.frakkintoasters.event.officials;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static xyz.zlatanov.frakkintoasters.state.board.Location.BRIG;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;

class MakeAdmiralEventProcessorTest extends EventTestHarness<MakeAdmiralEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(SAUL_TIGH);
        player(2).character(CAVIL);
    }

    @Test
    void shouldChangeTheAdmiral() {
        execute(new MakeAdmiralEvent(SAUL_TIGH));
        assertEquals(SAUL_TIGH, admiral());
    }

    // Cylon Leaders hold no titles (rule 6.2.3.3).
    @Test
    void notACylonLeader() {
        assertInvalid(new MakeAdmiralEvent(CAVIL));
    }

    // The Admiral loses the title in the "Brig" (rule 4.4.2.3), so a character sent there after the event was queued
    // doesn't take it (F-c3d2d0).
    @Test
    void notACharacterInTheBrig() {
        moveTo(BRIG, SAUL_TIGH);

        execute(new MakeAdmiralEvent(SAUL_TIGH));

        assertNull(admiral());
    }

    @Test
    void notACharacterNobodyPlays() {
        assertInvalid(new MakeAdmiralEvent(LEE_ADAMA));
    }
}
