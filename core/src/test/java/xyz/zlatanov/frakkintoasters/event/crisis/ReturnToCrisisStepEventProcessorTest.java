package xyz.zlatanov.frakkintoasters.event.crisis;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ReturnToCrisisStepEventProcessorTest extends EventTestHarness<ReturnToCrisisStepEvent> {

    @Test
    void shouldMakeTheTurnReturnAfterTheActivateCylonShipsStep() {
        execute(new ReturnToCrisisStepEvent());

        assertTrue(game.turn().returnsToCrisisStep());
    }
}
