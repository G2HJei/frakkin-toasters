package xyz.zlatanov.frakkintoasters.operation;

import lombok.val;
import xyz.zlatanov.frakkintoasters.engine.journal.FactRecorder;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.BoardFact.PursuitAdvanced;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.BoardFact.PursuitReset;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.DeckFact.CardsAddedOnTop;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.ShipFact.BasestarDamageCleared;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.ShipFact.ShipMoved;
import xyz.zlatanov.frakkintoasters.state.damage.BasestarDamage;
import xyz.zlatanov.frakkintoasters.state.ship.CylonShip;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;
import xyz.zlatanov.frakkintoasters.state.ship.Basestar;
import xyz.zlatanov.frakkintoasters.state.ship.HeavyRaider;
import xyz.zlatanov.frakkintoasters.state.ship.Raider;
import xyz.zlatanov.frakkintoasters.state.ship.Ship;
import xyz.zlatanov.frakkintoasters.state.ship.ShipType;
import xyz.zlatanov.frakkintoasters.state.track.Pursuit;

import static xyz.zlatanov.frakkintoasters.state.board.CylonFleetBoard.MOVE_TO_FLEET_MAP;
import static xyz.zlatanov.frakkintoasters.state.board.CylonFleetBoard.MOVE_TO_GALACTICA_MAP;
import static xyz.zlatanov.frakkintoasters.state.deck.DeckId.BASESTAR_DAMAGE;
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

    /**
     * Moves the Cylon ships in space areas of the main game board to the corresponding space areas of the Cylon Fleet board
     * when the fleet jumps. The basestar damage tokens go back into the pile of unused ones, randomly mixed (rule 12.1.1).
     */
    public void moveShipsFromGalactica() {
        val damage = new java.util.ArrayList<BasestarDamage>();
        galacticaBoard().shipsInSpace(CylonShip.class).forEach(ship -> {
            if (ship instanceof Basestar basestar && !basestar.damage().isEmpty()) {
                damage.addAll(basestar.damage());
                record(new BasestarDamageCleared(ship.id()));
            }
            record(new ShipMoved(ship.id(), GALACTICA_SPACE, CYLON_FLEET_SPACE, MOVE_TO_FLEET_MAP.get(galacticaBoard().locate(ship))));
        });
        if (!damage.isEmpty()) {
            record(new CardsAddedOnTop(BASESTAR_DAMAGE, damage));
            shuffle(BASESTAR_DAMAGE);
        }
    }

    public void moveShipsToGalactica() {
        cylonFleetBoard().shipsInSpace().forEach(this::moveToGalactica);
    }

    public void moveShipsToGalactica(Location from) {
        cylonFleetBoard().shipsIn(from).forEach(s -> moveToGalactica(s, from));
    }

    private void moveToGalactica(Ship ship, Location cylonFleetLocation) {
        record(new ShipMoved(ship.id(), CYLON_FLEET_SPACE, GALACTICA_SPACE, MOVE_TO_GALACTICA_MAP.get(cylonFleetLocation)));
    }
}
