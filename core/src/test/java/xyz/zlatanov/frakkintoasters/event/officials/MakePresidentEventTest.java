package xyz.zlatanov.frakkintoasters.event.officials;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.event.EventTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.character.Character.GAIUS_BALTAR;

class MakePresidentEventTest extends EventTest {

    @Test
    void shouldChangeThePresident() {
        executeAndAssertNoFollowup(new MakePresidentEvent(GAIUS_BALTAR));
        assertEquals(GAIUS_BALTAR, president());
    }

}