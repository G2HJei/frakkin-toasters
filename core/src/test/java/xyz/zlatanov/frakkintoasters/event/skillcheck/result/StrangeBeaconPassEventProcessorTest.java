package xyz.zlatanov.frakkintoasters.event.skillcheck.result;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.ship.DestroyCylonShipsEvent;

import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;

// "Choose 1 space area on the main game board and remove all Cylon ships in that area."
class StrangeBeaconPassEventProcessorTest extends EventTestHarness<StrangeBeaconPassEvent> {

    @Test
    void shouldLetTheCurrentPlayerChooseAnAreaWithCylonShips() {
        raiderAt(GALACTICA_SPACE_5_OCLOCK);
        basestarAt(GALACTICA_SPACE_9_OCLOCK);
        viperAt(GALACTICA_SPACE_7_OCLOCK);

        execute(new StrangeBeaconPassEvent());

        assertFollowup(one(new DestroyCylonShipsEvent(GALACTICA_SPACE_5_OCLOCK),
                new DestroyCylonShipsEvent(GALACTICA_SPACE_9_OCLOCK)));
    }

    @Test
    void shouldAskNobodyWithoutCylonShips() {
        viperAt(GALACTICA_SPACE_7_OCLOCK);

        execute(new StrangeBeaconPassEvent());
    }
}
