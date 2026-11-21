package xyz.zlatanov.frakkintoasters.event.action.skillcard;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.board.GainResourceEvent;
import xyz.zlatanov.frakkintoasters.event.ship.DestroyRaptorEvent;

import static xyz.zlatanov.frakkintoasters.state.board.Resource.FUEL;

class ScoutForFuelResultEventProcessorTest extends EventTestHarness<ScoutForFuelResultEvent> {

    @Test
    void onFourTheFleetGainsFuel() {
        rolled(4);

        execute(new ScoutForFuelResultEvent());

        assertFollowup(new GainResourceEvent(FUEL, 1));
    }

    @Test
    void lowerTheRaptorIsDestroyed() {
        rolled(3);

        execute(new ScoutForFuelResultEvent());

        assertFollowup(new DestroyRaptorEvent());
    }
}
