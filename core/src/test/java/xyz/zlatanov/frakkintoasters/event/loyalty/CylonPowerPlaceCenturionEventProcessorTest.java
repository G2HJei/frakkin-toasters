package xyz.zlatanov.frakkintoasters.event.loyalty;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.board.PlaceCenturionsEvent;

class CylonPowerPlaceCenturionEventProcessorTest extends EventTestHarness<CylonPowerPlaceCenturionEvent> {

    @Test
    void shouldPlaceACenturion() {
        execute(new CylonPowerPlaceCenturionEvent(1));

        assertFollowup(new PlaceCenturionsEvent(1));
    }
}
