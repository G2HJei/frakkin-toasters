package xyz.zlatanov.frakkintoasters.event.ship;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.state.ship.Raider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.board.Location.GALACTICA_SPACE_2_OCLOCK;

class PlaceRaidersBehindEventProcessorTest extends EventTestHarness<PlaceRaidersBehindEvent> {

    // "Place 3 raiders behind Galactica."
    @Test
    void shouldPlaceTheRaidersBehindGalactica() {
        execute(new PlaceRaidersBehindEvent(3));

        assertShipCount(GALACTICA_SPACE_2_OCLOCK, Raider.class, 3);
    }

    @Test
    void shouldNotPlaceNegativeCounts() {
        assertInvalid(new PlaceRaidersBehindEvent(-1));
    }
}
