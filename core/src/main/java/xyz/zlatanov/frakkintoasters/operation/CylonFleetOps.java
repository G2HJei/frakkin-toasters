package xyz.zlatanov.frakkintoasters.operation;

import lombok.val;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;
import xyz.zlatanov.frakkintoasters.state.ship.Ship;
import xyz.zlatanov.frakkintoasters.state.ship.ShipType;
import xyz.zlatanov.frakkintoasters.state.track.Pursuit;

import java.util.HashMap;
import java.util.Optional;

import static xyz.zlatanov.frakkintoasters.state.board.CylonFleetBoard.MOVE_TO_GALACTICA_MAP;

public class CylonFleetOps extends GameOps {

    CylonFleetOps(Game game) {
        super(game);
    }

    public Pursuit advancePursuit() {
        return cylonFleetBoard().advancePursuit();
    }

    public void resetPursuit() {
        cylonFleetBoard().resetPursuit();
    }

    public void place(ShipType shipType, Location location) {
        val cylonShips = game.cylonShips();
        Optional<? extends Ship> ship = switch (shipType) {
            case RAIDER -> cylonShips.raider();
            case HEAVY_RAIDER -> cylonShips.heavyRaider();
            case BASESTAR -> cylonShips.basestar();
            default -> throw new FrakCallTheAdmiralException();
        };
        cylonFleetBoard().place(location, ship.orElseThrow(FrakCallTheAdmiralException::new));
    }

    public void moveShipsToGalactica() {
        new HashMap<>(cylonFleetBoard().shipsInSpace())
                .forEach(this::moveToGalactica);
    }

    public void moveShipsToGalactica(ShipType shipType, Location from) {
        cylonFleetBoard()
                .shipsIn(from).stream()
                .filter(s -> ShipType.of(s.getClass()).equals(shipType))
                .forEach(s -> moveToGalactica(s, from));
    }

    private void moveToGalactica(Ship ship, Location cylonFleetLocation) {
        cylonFleetBoard().remove(ship);
        galacticaBoard().place(MOVE_TO_GALACTICA_MAP.get(cylonFleetLocation), ship);
    }
}
