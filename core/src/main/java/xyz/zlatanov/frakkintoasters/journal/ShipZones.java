package xyz.zlatanov.frakkintoasters.journal;

import lombok.val;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;
import xyz.zlatanov.frakkintoasters.state.ship.*;

import java.util.Collection;

/**
 * Takes a ship (or a centurion) out of a zone by id and puts it in another zone. Used by {@link FactApplier}.
 */
final class ShipZones {

    private ShipZones() {
    }

    static void move(Game game, int id, ShipZone from, ShipZone to, Location location) {
        add(game, to, remove(game, from, id), location);
    }

    private static Object remove(Game game, ShipZone zone, int id) {
        val galactica = game.boards().galactica();
        switch (zone) {
            case CYLON_SUPPLY:
                return game.cylonShips().take(id);
            case CENTURION_SUPPLY:
                return game.cylonShips().takeCenturion(id);
            case CIVILIAN_DECK: {
                val ship = find(game.decks().civilianShips().cards(), id);
                game.decks().civilianShips().remove(ship);
                return ship;
            }
            case RESERVES: {
                val ship = find(galactica.reserves(), id);
                galactica.removeFromReserves(ship);
                return ship;
            }
            case DAMAGED: {
                val ship = find(galactica.damagedShips(), id);
                galactica.removeFromDamagedShips(ship);
                return ship;
            }
            case GALACTICA_SPACE: {
                val ship = find(galactica.shipsInSpace().keySet(), id);
                galactica.remove(ship);
                return ship;
            }
            case CYLON_FLEET_SPACE: {
                val fleet = game.boards().cylonFleet();
                val ship = find(fleet.shipsInSpace().keySet(), id);
                fleet.remove(ship);
                return ship;
            }
            case BOARDING_PARTY:
                return galactica.destroyCenturion(id);
            default:
                throw new FrakCallTheAdmiralException();
        }
    }

    private static void add(Game game, ShipZone zone, Object piece, Location location) {
        val galactica = game.boards().galactica();
        switch (zone) {
            case CYLON_SUPPLY -> game.cylonShips().returned((Ship) piece);
            case CENTURION_SUPPLY -> game.cylonShips().returnedCenturion((Centurion) piece);
            case CIVILIAN_DECK -> game.decks().civilianShips().addOnTop((CivilianShip) piece);
            case RESERVES -> galactica.addToReserves((Ship) piece);
            case DAMAGED -> galactica.addToDamagedShips((Viper) piece);
            case GALACTICA_SPACE -> galactica.place(location, (Ship) piece);
            case CYLON_FLEET_SPACE -> game.boards().cylonFleet().place(location, (Ship) piece);
            case BOARDING_PARTY -> galactica.boardGalactica((Centurion) piece);
            case REMOVED -> game.removedComponents().add(piece);
        }
    }

    private static <T extends Ship> T find(Collection<T> ships, int id) {
        return ships.stream()
                .filter(s -> s.id() == id)
                .findFirst()
                .orElseThrow(FrakCallTheAdmiralException::new);
    }
}
