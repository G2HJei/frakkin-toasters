package xyz.zlatanov.frakkintoasters.event.ship;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;

class DestroyCylonShipsEventProcessorTest extends EventTestHarness<DestroyCylonShipsEvent> {

    @Test
    void shouldDestroyAllTheCylonShipsInTheArea() {
        raiderAt(GALACTICA_SPACE_5_OCLOCK);
        heavyRaiderAt(GALACTICA_SPACE_5_OCLOCK);
        basestarAt(GALACTICA_SPACE_5_OCLOCK);

        execute(new DestroyCylonShipsEvent(GALACTICA_SPACE_5_OCLOCK));

        assertNoShips(GALACTICA_SPACE_5_OCLOCK);
    }

    @Test
    void shouldLeaveOtherShipsAndOtherAreasAlone() {
        val raider = raiderAt(GALACTICA_SPACE_7_OCLOCK);
        val viper = viperAt(GALACTICA_SPACE_5_OCLOCK);
        civilianShipAt(GALACTICA_SPACE_5_OCLOCK);

        execute(new DestroyCylonShipsEvent(GALACTICA_SPACE_5_OCLOCK));

        assertEquals(GALACTICA_SPACE_7_OCLOCK, locate(raider));
        assertEquals(GALACTICA_SPACE_5_OCLOCK, locate(viper));
        assertShipCount(GALACTICA_SPACE_5_OCLOCK, 2);
    }

    @Test
    void shouldNotTakeNoArea() {
        assertInvalid(new DestroyCylonShipsEvent(null));
    }

    @Test
    void shouldNotTakeAnAreaOfTheCylonFleetBoard() {
        assertInvalid(new DestroyCylonShipsEvent(CYLON_FLEET_SPACE_1));
    }

    @Test
    void shouldNotTakeALocationThatIsNotAnArea() {
        assertInvalid(new DestroyCylonShipsEvent(COMMAND));
    }
}
