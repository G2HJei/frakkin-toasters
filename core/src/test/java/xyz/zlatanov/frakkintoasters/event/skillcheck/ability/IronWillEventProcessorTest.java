package xyz.zlatanov.frakkintoasters.event.skillcheck.ability;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IronWillEventProcessorTest extends EventTestHarness<IronWillEvent> {

    @Test
    void shouldDecreaseMorale() {
        execute(new IronWillEvent());
        assertEquals(9, galacticaBoard.morale());
    }
}