package xyz.zlatanov.frakkintoasters.operation;

import lombok.val;
import xyz.zlatanov.frakkintoasters.engine.journal.FactRecorder;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.DeckFact.CardsAddedOnTop;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.DeckFact.CardsDiscarded;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.DeckFact.CardsDrawn;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.DeckFact.DeckShuffled;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.BoardFact.ResourceDecreased;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.Fact;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.GameFact.LossReduced;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.ShipFact.ShipMoved;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.board.CylonFleetBoard;
import xyz.zlatanov.frakkintoasters.state.board.GalacticaBoard;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.board.LocationsArea;
import xyz.zlatanov.frakkintoasters.state.board.Resource;
import xyz.zlatanov.frakkintoasters.state.card.MutinyCard;
import xyz.zlatanov.frakkintoasters.state.damage.BasestarDamage;
import xyz.zlatanov.frakkintoasters.state.damage.GalacticaDamage;
import xyz.zlatanov.frakkintoasters.state.damage.PegasusDamage;
import xyz.zlatanov.frakkintoasters.state.deck.Deck;
import xyz.zlatanov.frakkintoasters.state.deck.DeckId;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;
import xyz.zlatanov.frakkintoasters.state.ship.CivilianShip;
import xyz.zlatanov.frakkintoasters.state.ship.CylonShip;
import xyz.zlatanov.frakkintoasters.state.ship.Ship;
import xyz.zlatanov.frakkintoasters.state.ship.ShipZone;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static xyz.zlatanov.frakkintoasters.state.deck.DeckId.*;
import static xyz.zlatanov.frakkintoasters.state.ship.ShipZone.CYLON_SUPPLY;
import static xyz.zlatanov.frakkintoasters.state.ship.ShipZone.GALACTICA_SPACE;

public abstract class AbstractOps {

    protected final Game         game;
    protected final FactRecorder recorder;

    protected AbstractOps(Game game, FactRecorder recorder) {
        this.game = game;
        this.recorder = recorder;
    }

    /**
     * Applies the fact to the game and appends it to the journal. The only way ops change state: ops compute a fact
     * from the current state and record it, and nothing else writes to the components.
     */
    protected void record(Fact fact) {
        recorder.record(fact);
    }

    /**
     * Combines the draw and discard piles of the deck and shuffles them. Logged as a {@code DeckShuffled} fact.
     */
    protected void shuffle(DeckId id) {
        Deck<Object> deck = game.decks().deck(id);
        List<Object> all = new ArrayList<>(deck.cards());
        all.addAll(deck.discardedCards());
        List<Object> order = game.randomness().shuffled(all);
        if (id == CIVILIAN_SHIPS) {
            order = dealCivilianShipIds(order);
        }
        record(new DeckShuffled(id, order));
    }

    // A ship keeps its id on the board, so the id of a ship that comes out of the deck must say nothing about its
    // cargo: every shuffle deals the ids of the ships in the deck to their cargo anew.
    private List<Object> dealCivilianShipIds(List<Object> ships) {
        val ids = game.randomness().shuffled(ships.stream().map(ship -> ((CivilianShip) ship).id()).toList());
        val dealt = new ArrayList<Object>();
        for (int i = 0; i < ships.size(); i++) {
            val cargo = (CivilianShip) ships.get(i);
            dealt.add(new CivilianShip(ids.get(i), cargo.fuelCost(), cargo.moraleCost(), cargo.populationCost()));
        }
        return dealt;
    }

    /**
     * Draws the top card. The card is in no pile afterwards, so the caller has to record where it went. When the draw
     * empties a deck that reshuffles its discard pile, the reshuffle is logged too.
     */
    protected <T> T draw(DeckId id) {
        Deck<T> deck = game.decks().deck(id);
        if (deck.autoShuffle() && deck.isEmpty() && deck.discardSize() > 0) {
            shuffle(id); // the cards were discarded after a draw emptied the pile
        }
        T card = deck.top(1).getFirst();
        record(new CardsDrawn(id, List.of(card)));
        if (deck.autoShuffle() && deck.isEmpty() && deck.discardSize() > 0) {
            shuffle(id);
        }
        return card;
    }

    protected <T> List<T> draw(DeckId id, int count) {
        List<T> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(draw(id));
        }
        return cards;
    }

    /**
     * Puts a card back in the deck it belongs to. Skill and mutiny cards go to the discard pile. Damage cards go on top
     * of their deck, and the caller shuffles it.
     */
    protected void discardToDeck(Object card) {
        switch (card) {
            case SkillCard skillCard ->
                    record(new CardsDiscarded(DeckId.of(skillCard.type().color()), List.of(skillCard)));
            case MutinyCard mutinyCard -> record(new CardsDiscarded(MUTINY, List.of(mutinyCard)));
            case GalacticaDamage damage -> record(new CardsAddedOnTop(GALACTICA_DAMAGE, List.of(damage)));
            case BasestarDamage damage -> record(new CardsAddedOnTop(BASESTAR_DAMAGE, List.of(damage)));
            case PegasusDamage damage -> record(new CardsAddedOnTop(PEGASUS_DAMAGE, List.of(damage)));
            case null, default -> throw new FrakCallTheAdmiralException();
        }
    }

    /**
     * Places a Cylon ship of the type in a space area of the main game board, as every effect that places one does
     * (rule 11.4.2). Nothing is placed when there is none of the type left.
     */
    protected Optional<Integer> takeFromSupply(Class<? extends CylonShip> type, Location location) {
        val fromSupply = game.cylonShips().next(type);
        if (fromSupply.isPresent()) {
            record(new ShipMoved(fromSupply.get().id(), CYLON_SUPPLY, GALACTICA_SPACE, location));
            return Optional.of(fromSupply.get().id());
        }
        // Out of ships in the supply: one from the Cylon Fleet board, the lowest-numbered space area first.
        val fromFleetBoard = LocationsArea.CYLON_FLEET_SPACE.locations().stream()
                .flatMap(area -> cylonFleetBoard().shipsIn(area, type).stream())
                .findFirst();
        fromFleetBoard.ifPresent(ship -> record(new ShipMoved(ship.id(), ShipZone.CYLON_FLEET_SPACE, GALACTICA_SPACE, location)));
        return fromFleetBoard.map(Ship::id);
    }

    /**
     * Loses the resource, as every loss does (rule 13.2). "Preventative Policy" makes the next loss of a resource this
     * turn 1 smaller.
     */
    protected void loseResource(Resource resource, int amount) {
        if (amount > 0 && game.turn().lossReductions().contains(resource)) {
            record(new LossReduced(resource));
            amount--;
        }
        if (amount > 0) {
            record(new ResourceDecreased(resource, amount));
        }
    }

    protected GalacticaBoard galacticaBoard() {
        return game.boards().galactica();
    }

    protected CylonFleetBoard cylonFleetBoard() {
        return game.boards().cylonFleet();
    }
}
