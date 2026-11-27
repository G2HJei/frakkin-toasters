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
        assertShipCount(GALACTICA_SPACE_9_OCLOCK, 2); // area 7-8 is in front of Galactica (rule 11.4)
    }

    // "He then moves all ships of all types in that space area" (rule 11.4.1.2).
    @Test
    void shouldTransferEveryShipOfTheSpaceArea() {
        raiderAt(CYLON_FLEET_SPACE_5_6);
        basestarAt(CYLON_FLEET_SPACE_5_6);
        outOfRaiders();

        execute(new PlaceShipOnCylonFleetBoardEvent(RAIDER));

        assertNoShips(CYLON_FLEET_SPACE_5_6);
    }

    // No raider on the Cylon Fleet board either: nothing moves.
    @Test
    void shouldMoveNothingWithoutAShipOfTheTypeOnTheCylonFleetBoard() {
        basestarAt(CYLON_FLEET_SPACE_5_6);
        outOfRaiders();

        execute(new PlaceShipOnCylonFleetBoardEvent(RAIDER));

        assertShipCount(CYLON_FLEET_SPACE_5_6, 1);
    }

    private void outOfRaiders() {
        cylonShips.raider().ifPresent(r -> outOfRaiders());
    }
}
