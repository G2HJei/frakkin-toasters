package xyz.zlatanov.frakkintoasters.event.officials;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.event.EventTestHarness;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.character.Character.SAUL_TIGH;

class MakeAdmiralEventProcessorTest extends EventTestHarness<MakeAdmiralEvent> {
    @Test
    void shouldChangeTheAdmiral() {
        execute(new MakeAdmiralEvent(SAUL_TIGH));
        assertNoFollowup();
        assertEquals(SAUL_TIGH, admiral());
    }
}