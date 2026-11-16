package xyz.zlatanov.frakkintoasters.event.ship;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.state.ship.CivilianShip;

import static xyz.zlatanov.frakkintoasters.state.board.Location.GALACTICA_SPACE_8_OCLOCK;

class PlaceCivilianShipsInFrontEventProcessorTest extends EventTestHarness<PlaceCivilianShipsInFrontEvent> {

    // "Place 1 civilian ship in front of Galactica."
    @Test
    void shouldPlaceTheCivilianShipInFrontOfGalactica() {
        execute(new PlaceCivilianShipsInFrontEvent(1));

        assertShipCount(GALACTICA_SPACE_8_OCLOCK, CivilianShip.class, 1);
    }

    @Test
    void shouldNotPlaceNegativeCounts() {
        assertInvalid(new PlaceCivilianShipsInFrontEvent(-1));
    }
}
