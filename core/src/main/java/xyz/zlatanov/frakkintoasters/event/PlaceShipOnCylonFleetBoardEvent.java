package xyz.zlatanov.frakkintoasters.event;

import lombok.val;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;
import xyz.zlatanov.frakkintoasters.state.ship.Ship;
import xyz.zlatanov.frakkintoasters.state.ship.ShipType;

import java.util.Map;

import static xyz.zlatanov.frakkintoasters.state.board.Location.*;

public record PlaceShipOnCylonFleetBoardEvent(ShipType cylonShipType) implements Event {

    @Override
    public Followup apply(Game game) {
        val ship = getShip(game);
        val placementLocation = placementMap.get(game.die().roll());
        game.boards().cylonFleet().place(placementLocation, ship);
        return Followup.NONE;
    }

    private Ship getShip(Game game) {
        val cylonShips = game.cylonShips();
        return switch (cylonShipType) {
            case RAIDER -> cylonShips.raider().orElseThrow();
            case HEAVY_RAIDER -> cylonShips.heavyRaider().orElseThrow();
            case BASESTAR -> cylonShips.basestar().orElseThrow();
            default -> throw new FrakCallTheAdmiralException();
        };
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
}
