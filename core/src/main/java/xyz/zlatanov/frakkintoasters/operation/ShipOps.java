package xyz.zlatanov.frakkintoasters.operation;

import lombok.val;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.ship.*;

import java.util.Optional;

import static xyz.zlatanov.frakkintoasters.state.board.Location.SICKBAY;
import static xyz.zlatanov.frakkintoasters.state.board.LocationsArea.GALACTICA_SPACE;

public class ShipOps extends GameOps {

    private final int shipId;

    ShipOps(Game game, int shipId) {
        super(game);
        this.shipId = shipId;
    }

    public void moveTo(Location location) {
        //todo move to cylon fleet board location when jumping
        assert GALACTICA_SPACE.locations().contains(location);
        val ship = galacticaBoard().shipInSpace(shipId, Ship.class);
        galacticaBoard()
                .remove(ship)
                .place(location, ship);
    }

    public void damage() {
        if (!damageInReserves()) {
            damageInSpace();
        }
    }

    public void destroy() {
        galacticaBoard()
                .reserves().stream()
                .filter(s -> s.id() == shipId)
                .findFirst()
                .ifPresentOrElse(this::destroyFromReserves,
                        this::destroyInSpace);
    }

    public void launchRaiders(int count) {
        val location = galacticaBoard().locate(basestar());
        for (int i = 0; i < count; i++) {
            game.cylonShips().raider()
                    .ifPresent(raider -> galacticaBoard().place(location, raider));
        }
    }

    public void launchHeavyRaider() {
        val location = galacticaBoard().locate(basestar());
        game.cylonShips().heavyRaider()
                .ifPresent(heavyRaider -> galacticaBoard().place(location, heavyRaider));
    }

    public void boardGalactica() {
        val heavyRaider = galacticaBoard().shipInSpace(shipId, HeavyRaider.class);
        game.cylonShips()
                .centurion()
                .ifPresent(centurion -> {
                    galacticaBoard().remove(heavyRaider);
                    game.cylonShips().returned(heavyRaider);
                    galacticaBoard().boardGalactica(centurion);
                });
    }

    private Basestar basestar() {
        return galacticaBoard().shipInSpace(shipId, Basestar.class);
    }

    private boolean damageInReserves() {
        val humanFighter = galacticaBoard().removeFromReserves(shipId);
        if (humanFighter.isEmpty()) {
            return false;
        }
        galacticaBoard().addToDamagedShips((Viper) humanFighter.get());
        return true;
    }

    private void damageInSpace() {
        val ship = galacticaBoard().shipInSpace(shipId, Ship.class);
        if (ship instanceof Basestar basestar) {
            basestar.damage(game.decks().basestarDamage().draw());
        } else {
            damageHumanFighter(galacticaBoard().shipInSpace(shipId, HumanFighter.class));
        }
    }

    private void damageHumanFighter(HumanFighter humanFighter) {
        galacticaBoard().remove(humanFighter);
        val pilot = humanFighter.pilot();
        if (pilot != null) {
            humanFighter.pilot(null);
            new CharacterOps(game, pilot).moveTo(SICKBAY);
        }
        if (humanFighter instanceof Viper viper) {
            galacticaBoard().addToDamagedShips(viper);
        } else {
            game.removedComponents().add(humanFighter);
        }
    }

    private void destroyFromReserves(Ship ship) {
        galacticaBoard().removeFromReserves(ship);
        game.removedComponents().add(ship);
    }

    private void destroyInSpace() {
        val galacticaBoard = galacticaBoard();
        val ship = galacticaBoard.shipInSpace(shipId, Ship.class);
        galacticaBoard.remove(ship);
        if (ship instanceof CylonShip) {
            destroyCylonShip(ship);
        } else if (ship instanceof HumanFighter humanFighter) {
            Optional.ofNullable(humanFighter.pilot())
                    .ifPresent(pilot -> new CharacterOps(game, pilot).moveTo(SICKBAY));
            destroyHumanShip(humanFighter);
        } else if (ship instanceof CivilianShip civilianShip) {
            destroyCivilianShip(civilianShip);
        } else {
            destroyHumanShip(ship);
        }
    }

    private void destroyCylonShip(Ship ship) {
        if (ship instanceof Basestar basestar) {
            game.decks().discard(basestar.damage());
            basestar.clearDamage();
        }
        game.cylonShips().returned(ship);
    }

    private void destroyHumanShip(Ship ship) {
        if (ship instanceof HumanFighter
                || ship instanceof Raptor) {
            game.removedComponents().add(ship);
        }
    }

    private void destroyCivilianShip(CivilianShip civilianShip) {
        galacticaBoard()
                .decreaseFuel(civilianShip.fuelCost())
                .decreaseMorale(civilianShip.moraleCost())
                .decreasePopulation(civilianShip.populationCost());
        game.removedComponents().add(civilianShip);
    }

    public void land(Location destination) {
        val ship = galacticaBoard().shipInSpace(shipId, HumanFighter.class);
        galacticaBoard().addToReserves(ship);
        character(ship.pilot()).moveTo(destination);
        ship.pilot(null);
    }

}
