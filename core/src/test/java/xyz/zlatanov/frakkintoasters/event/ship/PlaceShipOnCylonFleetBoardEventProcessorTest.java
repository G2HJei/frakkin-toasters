package xyz.zlatanov.frakkintoasters.event.ship;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;

import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.rollDie;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.RAIDER;

class PlaceShipOnCylonFleetBoardEventProcessorTest extends EventTestHarness<PlaceShipOnCylonFleetBoardEvent> {

    @Test
    void shouldRollForThePlacement() {
        execute(new PlaceShipOnCylonFleetBoardEvent(RAIDER));

        assertFollowup(rollDie(new PlaceShipOnCylonFleetBoardResultEvent(RAIDER)));
    }

    @Test
    void shouldTransferShipsToMainBoard() {
        raiderAt(CYLON_FLEET_SPACE_7_8);
        raiderAt(CYLON_FLEET_SPACE_7_8);
        raiderAt(CYLON_FLEET_SPACE_1);
        outOfRaiders();

        execute(new PlaceShipOnCylonFleetBoardEvent(RAIDER));

        assertNoShips(CYLON_FLEET_SPACE_7_8);
        assertShipCount(CYLON_FLEET_SPACE_1, 1);
        assertShipCount(GALACTICA_SPACE_12_OCLOCK, 2);
    }

    private void outOfRaiders() {
        cylonShips.raider().ifPresent(r -> outOfRaiders());
    }
}
