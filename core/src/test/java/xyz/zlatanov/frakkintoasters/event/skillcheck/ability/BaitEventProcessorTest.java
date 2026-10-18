package xyz.zlatanov.frakkintoasters.event.skillcheck.ability;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.state.ship.CivilianShip;

import static xyz.zlatanov.frakkintoasters.state.board.Location.GALACTICA_SPACE_2_OCLOCK;

class BaitEventProcessorTest extends EventTestHarness<BaitEvent> {

    @Test
    void shouldPlaceCivilianShipBehindGalactica() {
        execute(new BaitEvent());
        assertShipCount(GALACTICA_SPACE_2_OCLOCK, CivilianShip.class, 1);
    }
}