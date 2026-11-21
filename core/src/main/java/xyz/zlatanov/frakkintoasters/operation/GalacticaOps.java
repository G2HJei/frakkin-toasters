package xyz.zlatanov.frakkintoasters.operation;

import lombok.val;
import xyz.zlatanov.frakkintoasters.engine.journal.FactRecorder;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.BoardFact.*;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.DeckFact.CardsAddedOnTop;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.DeckFact.CardsAddedToBottom;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.DeckFact.CardsDiscarded;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.GameFact.DestinationReached;
import xyz.zlatanov.frakkintoasters.state.card.DestinationCard;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.ShipFact.ShipMoved;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.board.GalacticaBoard;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.board.Resource;
import xyz.zlatanov.frakkintoasters.state.damage.GalacticaDamage;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;
import xyz.zlatanov.frakkintoasters.state.ship.*;

import java.util.List;
import java.util.Optional;

import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.board.Resource.MORALE;
import static xyz.zlatanov.frakkintoasters.state.board.Resource.POPULATION;
import static xyz.zlatanov.frakkintoasters.state.deck.DeckId.GALACTICA_DAMAGE;
import static xyz.zlatanov.frakkintoasters.state.deck.DeckId.DESTINATION;
import static xyz.zlatanov.frakkintoasters.state.ship.ShipZone.*;

public class GalacticaOps extends BattlestarOps {

    GalacticaOps(Game game, FactRecorder recorder) {
        super(game, recorder);
    }

    // todo certain effects destroy a random civ ship ( Misjump destination / Helena Cain miracle ability ). To handle this outside of ShipOps, probably GalacticaOps

    public void damage() {
        resolveDamage(draw(GALACTICA_DAMAGE));
    }

    /**
     * @return the drawn damage tokens, which are in no pile until resolved or returned
     */
    public List<GalacticaDamage> drawDamage(int count) {
        return draw(GALACTICA_DAMAGE, count);
    }

    public void returnDamage(GalacticaDamage galacticaDamage) {
        record(new CardsAddedOnTop(GALACTICA_DAMAGE, List.of(galacticaDamage)));
        shuffle(GALACTICA_DAMAGE);
    }

    public void resolveDamage(GalacticaDamage galacticaDamage) {
        if (GalacticaDamage.FUEL == galacticaDamage) {
            loseResource(Resource.FUEL, 1);
            record(new CardsAddedOnTop(GALACTICA_DAMAGE, List.of(GalacticaDamage.FUEL)));
            shuffle(GALACTICA_DAMAGE);
        } else if (GalacticaDamage.FOOD == galacticaDamage) {
            loseResource(Resource.FOOD, 1);
            record(new CardsAddedOnTop(GALACTICA_DAMAGE, List.of(GalacticaDamage.FOOD)));
            shuffle(GALACTICA_DAMAGE);
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
        for (int i = 0; i < 3; i++) {
            takeFromSupply(Raider.class, GALACTICA_SPACE_8_OCLOCK);
        }
    }

    public void decreasePopulation(int populationLost) {
        loseResource(POPULATION, populationLost);
    }

    public void decreaseMorale(int moraleLost) {
        loseResource(MORALE, moraleLost);
    }

    public void decreaseResource(Resource resource, int amount) {
        loseResource(resource, amount);
    }

    public void increaseResource(Resource resource, int amount) {
        record(new ResourceIncreased(resource, amount));
    }

    public void increaseJumpPreparation() {
        record(new JumpPreparationIncreased());
    }

    public void decreaseJumpPreparation() {
        record(new JumpPreparationDecreased());
    }

    public void resetJumpPreparation() {
        record(new JumpPreparationReset());
    }

    /**
     * @return the drawn Destination Cards, which are in no pile until placed or returned
     */
    public List<DestinationCard> drawDestinationCards(int count) {
        return draw(DESTINATION, count);
    }

    public void returnDestinationCardToBottom(DestinationCard card) {
        record(new CardsAddedToBottom(DESTINATION, List.of(card)));
    }

    public void discardDestinationCard(DestinationCard card) {
        record(new CardsDiscarded(DESTINATION, List.of(card)));
    }

    /**
     * Places the card next to the Objective Card, where its distance counts.
     */
    public void reachDestination(DestinationCard card) {
        record(new DestinationReached(card));
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

    /**
     * Destroys the civilian ship on top of the civilian ship deck, as when one is drawn to be destroyed (rule 13.6).
     */
    public void destroyDrawnCivilianShip() {
        ship(game.decks().civilianShips().top(1).getFirst().id()).destroyFromDeck();
    }

    public Optional<Integer> placeBasestar(Location location) {
        return takeFromSupply(Basestar.class, location);
    }

    /**
     * Places a centurion at the start of the Boarding Party track, if one is left.
     */
    public void placeCenturion() {
        game.cylonShips().nextCenturion()
                .ifPresent(centurion -> record(new ShipMoved(centurion.id(), CENTURION_SUPPLY, BOARDING_PARTY, null)));
    }

    @Override
    protected GalacticaBoard board() {
        return galacticaBoard();
    }

}
