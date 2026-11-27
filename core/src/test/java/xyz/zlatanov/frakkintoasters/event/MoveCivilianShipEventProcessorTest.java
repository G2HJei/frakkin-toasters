package xyz.zlatanov.frakkintoasters.event;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.state.ship.CivilianShip;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;

class MoveCivilianShipEventProcessorTest extends EventTestHarness<MoveCivilianShipEvent> {

    // "Communications": a civilian ship moves to an adjacent space area.
    @Test
    void aShipMovesToAnAdjacentArea() {
        CivilianShip ship = civilianShipAt(GALACTICA_SPACE_3_OCLOCK);

        execute(new MoveCivilianShipEvent(ship.id(), GALACTICA_SPACE_5_OCLOCK));

        assertEquals(GALACTICA_SPACE_5_OCLOCK, locate(ship));
    }

    @Test
    void notFurther() {
        CivilianShip ship = civilianShipAt(GALACTICA_SPACE_3_OCLOCK);

        assertInvalid(new MoveCivilianShipEvent(ship.id(), GALACTICA_SPACE_7_OCLOCK));
    }

    @Test
    void onlyACivilianShipInSpace() {
        assertInvalid(new MoveCivilianShipEvent(999, GALACTICA_SPACE_5_OCLOCK));
    }
}
