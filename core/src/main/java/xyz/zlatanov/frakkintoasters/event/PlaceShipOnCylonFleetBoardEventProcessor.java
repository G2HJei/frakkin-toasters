package xyz.zlatanov.frakkintoasters.event;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.board.CylonFleetBoard;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;
import xyz.zlatanov.frakkintoasters.state.ship.HeavyRaider;
import xyz.zlatanov.frakkintoasters.state.ship.Ship;

import java.util.Map;

import static xyz.zlatanov.frakkintoasters.state.board.Location.*;

public class PlaceShipOnCylonFleetBoardEventProcessor extends EventProcessor<PlaceShipOnCylonFleetBoardEvent> {

    @Override
    public Followup process() {
        val ship = getShip();
        val placementLocation = placementMap.get(game.die().roll());
        game.boards().cylonFleet().place(placementLocation, ship);
        return Followup.NONE;
    }

    private Ship getShip() {
        val cylonShips = game.cylonShips();
        return switch (event.cylonShipType()) {
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

    //todo consider out of ships case
    private Followup placeOnCylonFleetBoard(Game game) {
        val cylonFleet = game.boards().cylonFleet();
        cylonFleet.advancePursuit();
        val target = CylonFleetBoard.spaceFromRoll(game.die().roll());
        game.cylonShips()
                .heavyRaider()
                .ifPresentOrElse(
                        ship -> cylonFleet.place(target, ship),
                        this::spillOutOfShips);
        return Followup.NONE;
    }

    private void spillOutOfShips() {
        val cylonFleet = game.boards().cylonFleet();
        val galactica = game.boards().galactica();
        for (int i = CylonFleetBoard.SPACE_AREAS.size() - 1; i >= 0; i--) {
            val space = CylonFleetBoard.SPACE_AREAS.get(i);
            val heavyRaiders = cylonFleet.shipsIn(space, HeavyRaider.class);

            if (!heavyRaiders.isEmpty()) {
                val destination = CylonFleetBoard.MOVE_TO_GALACTICA_MAP.get(space);
                for (val hr : heavyRaiders) {
                    cylonFleet.remove(hr);
                    galactica.place(destination, hr);
                }
                return;
            }
        }
    }
}
