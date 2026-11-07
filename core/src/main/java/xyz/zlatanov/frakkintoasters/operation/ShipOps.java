package xyz.zlatanov.frakkintoasters.operation;

import lombok.val;
import xyz.zlatanov.frakkintoasters.engine.journal.FactRecorder;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.BoardFact.ResourceDecreased;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.DeckFact.CardsAddedOnTop;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.ShipFact.BasestarDamageAttached;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.ShipFact.BasestarDamageCleared;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.ShipFact.PilotChanged;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.ShipFact.ShipMoved;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.damage.BasestarDamage;
import xyz.zlatanov.frakkintoasters.state.ship.*;

import java.util.ArrayList;

import static xyz.zlatanov.frakkintoasters.state.board.Location.SICKBAY;
import xyz.zlatanov.frakkintoasters.state.board.LocationsArea;
import static xyz.zlatanov.frakkintoasters.state.board.Resource.*;
import static xyz.zlatanov.frakkintoasters.state.deck.DeckId.BASESTAR_DAMAGE;
import static xyz.zlatanov.frakkintoasters.state.deck.DeckId.CIVILIAN_SHIPS;
import static xyz.zlatanov.frakkintoasters.state.ship.ShipZone.*;

public class ShipOps extends GameOps {

    private final int shipId;

    ShipOps(Game game, FactRecorder recorder, int shipId) {
        super(game, recorder);
        this.shipId = shipId;
    }

    public void moveTo(Location location) {
        //todo move to cylon fleet board location when jumping
        assert LocationsArea.GALACTICA_SPACE.locations().contains(location);
        galacticaBoard().shipInSpace(shipId, Ship.class); // fails when the ship is not in space
        record(new ShipMoved(shipId, GALACTICA_SPACE, GALACTICA_SPACE, location));
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
                .ifPresentOrElse(s -> record(new ShipMoved(shipId, RESERVES, REMOVED, null)),
                        this::destroyInSpace);
    }

    public void launchRaiders(int count) {
        val location = galacticaBoard().locate(basestar());
        for (int i = 0; i < count; i++) {
            takeFromSupply(Raider.class, location);
        }
    }

    public void launchHeavyRaider() {
        val location = galacticaBoard().locate(basestar());
        takeFromSupply(HeavyRaider.class, location);
    }

    public void boardGalactica() {
        val heavyRaider = galacticaBoard().shipInSpace(shipId, HeavyRaider.class);
        game.cylonShips()
                .nextCenturion()
                .ifPresent(centurion -> {
                    record(new ShipMoved(heavyRaider.id(), GALACTICA_SPACE, CYLON_SUPPLY, null));
                    record(new ShipMoved(centurion.id(), CENTURION_SUPPLY, BOARDING_PARTY, null));
                });
    }

    private Basestar basestar() {
        return galacticaBoard().shipInSpace(shipId, Basestar.class);
    }

    private boolean damageInReserves() {
        val humanFighter = galacticaBoard().reserves().stream()
                .filter(s -> s.id() == shipId && s instanceof HumanFighter)
                .findFirst();
        if (humanFighter.isEmpty()) {
            return false;
        }
        record(new ShipMoved(shipId, RESERVES, DAMAGED, null));
        return true;
    }

    private void damageInSpace() {
        val ship = galacticaBoard().shipInSpace(shipId, Ship.class);
        if (ship instanceof Basestar) {
            BasestarDamage card = draw(BASESTAR_DAMAGE);
            record(new BasestarDamageAttached(shipId, card));
        } else {
            damageHumanFighter(galacticaBoard().shipInSpace(shipId, HumanFighter.class));
        }
    }

    private void damageHumanFighter(HumanFighter humanFighter) {
        handlePilot(humanFighter);
        record(new ShipMoved(shipId, GALACTICA_SPACE, humanFighter instanceof Viper ? DAMAGED : REMOVED, null));
    }

    private void destroyInSpace() {
        val ship = galacticaBoard().shipInSpace(shipId, Ship.class);
        if (ship instanceof CylonShip) {
            destroyCylonShip(ship);
        } else if (ship instanceof HumanFighter humanFighter) {
            handlePilot(humanFighter);
            record(new ShipMoved(shipId, GALACTICA_SPACE, REMOVED, null));
        } else if (ship instanceof CivilianShip civilianShip) {
            destroyCivilianShip(civilianShip);
        } else {
            record(new ShipMoved(shipId, GALACTICA_SPACE, REMOVED, null));
        }
    }

    private void handlePilot(HumanFighter humanFighter) {
        val pilot = humanFighter.pilot();
        if (pilot != null) {
            record(new PilotChanged(shipId, null));
            player(pilot).moveTo(SICKBAY);
        }
    }

    private void destroyCylonShip(Ship ship) {
        if (ship instanceof Basestar basestar && !basestar.damage().isEmpty()) {
            val damage = new ArrayList<>(basestar.damage());
            record(new BasestarDamageCleared(shipId));
            record(new CardsAddedOnTop(BASESTAR_DAMAGE, damage));
            shuffle(BASESTAR_DAMAGE);
        }
        record(new ShipMoved(shipId, GALACTICA_SPACE, CYLON_SUPPLY, null));
    }

    private void destroyCivilianShip(CivilianShip civilianShip) {
        decrease(FUEL, civilianShip.fuelCost());
        decrease(MORALE, civilianShip.moraleCost());
        decrease(POPULATION, civilianShip.populationCost());
        record(new ShipMoved(shipId, GALACTICA_SPACE, REMOVED, null));
    }

    private void decrease(xyz.zlatanov.frakkintoasters.state.board.Resource resource, int amount) {
        if (amount > 0) {
            record(new ResourceDecreased(resource, amount));
        }
    }

    public void land(Location destination) {
        val ship = galacticaBoard().shipInSpace(shipId, HumanFighter.class);
        val pilot = ship.pilot();
        record(new PilotChanged(shipId, null));
        record(new ShipMoved(shipId, GALACTICA_SPACE, RESERVES, null));
        player(pilot).moveTo(destination);
    }

    public void escort() {
        galacticaBoard().shipInSpace(shipId, CivilianShip.class); // fails when it is not a civilian ship in space
        record(new ShipMoved(shipId, GALACTICA_SPACE, CIVILIAN_DECK, null));
        shuffle(CIVILIAN_SHIPS);
    }
}
