package xyz.zlatanov.frakkintoasters.event.skillcheck.ability;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.state.ship.Raider;

import static xyz.zlatanov.frakkintoasters.state.board.Location.GALACTICA_SPACE_8_OCLOCK;

class DradisContactEventProcessorTest extends EventTestHarness<DradisContactEvent> {

    @Test
    void shouldPlaceRaiders() {
        execute(new DradisContactEvent());
        assertShipCount(GALACTICA_SPACE_8_OCLOCK, Raider.class, 2);
    }

}