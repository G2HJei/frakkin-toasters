package xyz.zlatanov.frakkintoasters.operation;

import lombok.val;
import xyz.zlatanov.frakkintoasters.engine.journal.FactRecorder;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.BoardFact.PursuitAdvanced;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.BoardFact.PursuitReset;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.ShipFact.ShipMoved;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;
import xyz.zlatanov.frakkintoasters.state.ship.Basestar;
import xyz.zlatanov.frakkintoasters.state.ship.HeavyRaider;
import xyz.zlatanov.frakkintoasters.state.ship.Raider;
import xyz.zlatanov.frakkintoasters.state.ship.Ship;
import xyz.zlatanov.frakkintoasters.state.ship.ShipType;
import xyz.zlatanov.frakkintoasters.state.track.Pursuit;

import java.util.HashMap;

import static xyz.zlatanov.frakkintoasters.state.board.CylonFleetBoard.MOVE_TO_GALACTICA_MAP;
import static xyz.zlatanov.frakkintoasters.state.ship.ShipZone.*;

public class CylonFleetOps extends GameOps {

    CylonFleetOps(Game game, FactRecorder recorder) {
        super(game, recorder);
    }

    public Pursuit advancePursuit() {
        record(new PursuitAdvanced());
        return cylonFleetBoard().pursuitTrack();
    }

    public void resetPursuit() {
        record(new PursuitReset());
    }

    public void place(ShipType shipType, Location location) {
        val cylonShips = game.cylonShips();
        val ship = switch (shipType) {
            case RAIDER -> cylonShips.next(Raider.class);
            case HEAVY_RAIDER -> cylonShips.next(HeavyRaider.class);
            case BASESTAR -> cylonShips.next(Basestar.class);
            default -> throw new FrakCallTheAdmiralException();
        };
        record(new ShipMoved(ship.orElseThrow(FrakCallTheAdmiralException::new).id(), CYLON_SUPPLY, CYLON_FLEET_SPACE, location));
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
        record(new ShipMoved(ship.id(), CYLON_FLEET_SPACE, GALACTICA_SPACE, MOVE_TO_GALACTICA_MAP.get(cylonFleetLocation)));
    }
}
