package xyz.zlatanov.frakkintoasters.operation;

import lombok.val;
import xyz.zlatanov.frakkintoasters.journal.fact.BoardFact.BoardingPartyAdvanced;
import xyz.zlatanov.frakkintoasters.journal.fact.BoardFact.ResourceDecreased;
import xyz.zlatanov.frakkintoasters.journal.fact.DeckFact.CardsAddedOnTop;
import xyz.zlatanov.frakkintoasters.journal.fact.ShipFact.ShipMoved;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.board.GalacticaBoard;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.damage.GalacticaDamage;
import xyz.zlatanov.frakkintoasters.state.deck.DeckId;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;
import xyz.zlatanov.frakkintoasters.state.ship.*;

import java.util.List;

import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.board.Resource.*;
import static xyz.zlatanov.frakkintoasters.state.damage.GalacticaDamage.FOOD;
import static xyz.zlatanov.frakkintoasters.state.damage.GalacticaDamage.FUEL;
import static xyz.zlatanov.frakkintoasters.state.ship.ShipZone.*;

public class GalacticaOps extends BattlestarOps {

    GalacticaOps(Game game) {
        super(game);
    }

    // todo certain effects destroy a random civ ship ( Misjump destination / Helena Cain miracle ability ). To handle this outside of ShipOps, probably GalacticaOps

    public void damage() {
        GalacticaDamage galacticaDamage = draw(DeckId.GALACTICA_DAMAGE);
        if (FUEL == galacticaDamage) {
            record(new ResourceDecreased(xyz.zlatanov.frakkintoasters.state.board.Resource.FUEL, 1));
            record(new CardsAddedOnTop(DeckId.GALACTICA_DAMAGE, List.of(FUEL)));
            shuffle(DeckId.GALACTICA_DAMAGE);
        } else if (FOOD == galacticaDamage) {
            record(new ResourceDecreased(xyz.zlatanov.frakkintoasters.state.board.Resource.FOOD, 1));
            record(new CardsAddedOnTop(DeckId.GALACTICA_DAMAGE, List.of(FOOD)));
            shuffle(DeckId.GALACTICA_DAMAGE);
        } else {
            damageLocation(Location.valueOf(galacticaDamage.name()));
        }
    }

    /**
     * Places the starting ships.
     */
    public void setUp() {
        val vipers = galacticaBoard().reserves().stream()
                .filter(s -> s.getClass() == Viper.class)
                .limit(2)
                .toList();
        record(new ShipMoved(vipers.get(0).id(), RESERVES, GALACTICA_SPACE, GALACTICA_SPACE_4_OCLOCK));
        record(new ShipMoved(vipers.get(1).id(), RESERVES, GALACTICA_SPACE, GALACTICA_SPACE_6_OCLOCK));
        placeCivilianShip(GALACTICA_SPACE_2_OCLOCK);
        placeCivilianShip(GALACTICA_SPACE_2_OCLOCK);
        takeFromSupply(Basestar.class, GALACTICA_SPACE_8_OCLOCK);
        for (int i = 0; i < 4; i++) {
            takeFromSupply(Raider.class, GALACTICA_SPACE_8_OCLOCK);
        }
    }

    public void decreasePopulation(int populationLost) {
        record(new ResourceDecreased(POPULATION, populationLost));
    }

    public void decreaseMorale(int moraleLost) {
        record(new ResourceDecreased(MORALE, moraleLost));
    }

    public void advanceBoardingParty() {
        record(new BoardingPartyAdvanced());
    }

    public void destroyCenturion(int centurionId) {
        record(new ShipMoved(centurionId, BOARDING_PARTY, CENTURION_SUPPLY, null));
    }

    public int launchHumanFighter(ShipType shipType, Location location, Integer unmannedViperId) {
        val shipClass = switch (shipType) {
            case VIPER, VIPER_MARK_VII, ASSAULT_RAPTOR -> shipType.shipClass();
            default -> throw new FrakCallTheAdmiralException();
        };
        val inReserves = galacticaBoard().reserves().stream()
                .filter(s -> s.getClass() == shipClass)
                .findFirst();
        if (inReserves.isPresent()) {
            val id = inReserves.get().id();
            record(new ShipMoved(id, RESERVES, GALACTICA_SPACE, location));
            return id;
        }
        // no such ship in reserves: the unmanned viper in space is relaunched from where it is
        val unmanned = galacticaBoard().shipInSpace(unmannedViperId, HumanFighter.class);
        record(new ShipMoved(unmanned.id(), GALACTICA_SPACE, GALACTICA_SPACE, location));
        return unmanned.id();
    }

    public void placeCivilianShip(Location location) {
        CivilianShip civ = game.decks().civilianShips().top(1).getFirst();
        record(new ShipMoved(civ.id(), CIVILIAN_DECK, GALACTICA_SPACE, location));
    }

    public void placeRaider(Location location) {
        takeFromSupply(Raider.class, location);
    }

    @Override
    protected GalacticaBoard board() {
        return galacticaBoard();
    }

}
