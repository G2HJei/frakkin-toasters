package xyz.zlatanov.frakkintoasters.event;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;
import xyz.zlatanov.frakkintoasters.state.ship.Ship;
import xyz.zlatanov.frakkintoasters.state.ship.ShipType;

import java.util.Map;
import java.util.Optional;
import java.util.TreeSet;

import static xyz.zlatanov.frakkintoasters.state.board.CylonFleetBoard.MOVE_TO_GALACTICA_MAP;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;

public class PlaceShipOnCylonFleetBoardEventProcessor extends EventProcessor<PlaceShipOnCylonFleetBoardEvent> {

    @Override
    public Followup process() {
        getShip().ifPresentOrElse(
                this::placeOnCylonFleetBoard,
                this::transferShipsToMainBoard);
        return Followup.NONE;
    }

    private Optional<? extends Ship> getShip() {
        val cylonShips = game.cylonShips();
        return switch (event.cylonShipType()) {
            case RAIDER -> cylonShips.raider();
            case HEAVY_RAIDER -> cylonShips.heavyRaider();
            case BASESTAR -> cylonShips.basestar();
            default -> throw new FrakCallTheAdmiralException();
        };
    }

    private void placeOnCylonFleetBoard(Ship ship) {
        val placementLocation = placementMap.get(rollDie());
        cylonFleetBoard.place(placementLocation, ship);
    }

    private void transferShipsToMainBoard() {
        val sourceLocation = findLocationToTransferFrom();
        val targetLocation = MOVE_TO_GALACTICA_MAP.get(sourceLocation);
        cylonFleetBoard
                .shipsIn(sourceLocation).stream()
                .filter(s -> ShipType.of(s.getClass()).equals(event.cylonShipType()))
                .forEach(s -> moveShip(s, targetLocation));
    }

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

    private Location findLocationToTransferFrom() {
        val locationsWithShip = new TreeSet<Location>();
        cylonFleetBoard
                .shipsInSpace()
                .entrySet()
                .stream()
                .filter(e -> ShipType.of(e.getKey().getClass()).equals(event.cylonShipType()))
                .map(Map.Entry::getValue)
                .forEach(locationsWithShip::add);
        return locationsWithShip.getLast();
    }

    private void moveShip(Ship ship, Location targetLocation) {
        // todo add utility method for this kind of remove > place action if used often enough
        cylonFleetBoard.remove(ship);
        galacticaBoard.place(targetLocation, ship);
    }

}
