package xyz.zlatanov.frakkintoasters.event.ship;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.board.Location;

import java.util.Map;
import java.util.TreeSet;

import static xyz.zlatanov.frakkintoasters.state.board.Location.*;

public class PlaceShipOnCylonFleetBoardEventProcessor extends EventProcessor<PlaceShipOnCylonFleetBoardEvent> {

    private static final Map<Integer, Location> placementMap = Map.of(
            1, CYLON_FLEET_SPACE_1,
            2, CYLON_FLEET_SPACE_2,
            3, CYLON_FLEET_SPACE_3,
            4, CYLON_FLEET_SPACE_4,
            5, CYLON_FLEET_SPACE_5_6,
            6, CYLON_FLEET_SPACE_5_6,
            7, CYLON_FLEET_SPACE_7_8,
            8, CYLON_FLEET_SPACE_7_8
    );

    @Override
    public Followup process() {
        val shipType = event.cylonShipType();
        if (game.cylonShips().available(shipType) > 0) {
            ops.cylonFleet().place(shipType, placementMap.get(ops.rollDie()));
        } else {
            ops.cylonFleet().moveShipsToGalactica(shipType, findLocationToTransferFrom());
        }
        return Followup.NONE;
    }

    private Location findLocationToTransferFrom() {
        val locationsWithShip = new TreeSet<Location>();
        cylonFleetBoard
                .shipsInSpace(event.cylonShipType().shipClass())
                .forEach(ship -> locationsWithShip.add(cylonFleetBoard.locate(ship)));
        return locationsWithShip.getLast();
    }

}
