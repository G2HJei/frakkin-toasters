package xyz.zlatanov.frakkintoasters.event.ship;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.board.Location;

import java.util.TreeSet;

import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.rollDie;

public class PlaceShipOnCylonFleetBoardEventProcessor extends EventProcessor<PlaceShipOnCylonFleetBoardEvent> {

    @Override
    public Followup process() {
        val shipType = event.cylonShipType();
        if (game.cylonShips().available(shipType) > 0) {
            return rollDie(new PlaceShipOnCylonFleetBoardResultEvent(shipType)); // the placement die roll (rule 11.4.1.1)
        }
        ops.cylonFleet().moveShipsToGalactica(shipType, findLocationToTransferFrom()); // out of ships (rule 11.4.1.2)
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
