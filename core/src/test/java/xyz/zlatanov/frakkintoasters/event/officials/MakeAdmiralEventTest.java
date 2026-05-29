package xyz.zlatanov.frakkintoasters.event.officials;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.event.EventTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.character.Character.SAUL_TIGH;

class MakeAdmiralEventTest extends EventTest {
    @Test
    void shouldChangeTheAdmiral() {
        execute(new MakeAdmiralEvent(SAUL_TIGH));
        assertEquals(SAUL_TIGH, game.admiral());
    }
}