package xyz.zlatanov.frakkintoasters.operation;

import lombok.RequiredArgsConstructor;
import lombok.val;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.ship.*;

import java.util.Optional;

import static xyz.zlatanov.frakkintoasters.state.board.Location.SICKBAY;

@RequiredArgsConstructor
public class ShipOperations {

    private final Game game;

    public void destroy(int shipId) {
        game.boards().galactica()
                .reserves().stream()
                .filter(s -> s.id() == shipId)
                .findFirst()
                .ifPresentOrElse(this::destroyFromReserves,
                        () -> destroyInSpace(shipId));
    }

    private void destroyFromReserves(Ship ship) {
        game.boards().galactica().reserves().remove(ship);
        game.removeComponent(ship);
    }

    private void destroyInSpace(int shipId) {
        val galacticaBoard = game.boards().galactica();
        val ship = galacticaBoard.shipInSpace(shipId, Ship.class);
        galacticaBoard.remove(ship);
        if (ship instanceof CylonShip) {
            destroyCylonShip(ship);
        } else if (ship instanceof HumanFighter humanFighter) {
            Optional.ofNullable(humanFighter.pilot())
                    .ifPresent(pilot -> game.moveTo(SICKBAY, pilot));
            destroyHumanShip(humanFighter);
        } else {
            destroyHumanShip(ship);
        }
    }

    private void destroyCylonShip(Ship ship) {
        game.cylonShips().returned(ship);
        if (ship instanceof Basestar bs) {
            game.decks().discard(bs.damage());
        }
    }

    private void destroyHumanShip(Ship ship) {
        if (ship instanceof HumanFighter
                || ship instanceof Raptor) {
            game.removeComponent(ship);
        }
        //todo civ ship
    }
}
