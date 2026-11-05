package xyz.zlatanov.frakkintoasters.operation;

import xyz.zlatanov.frakkintoasters.journal.FactApplier;
import xyz.zlatanov.frakkintoasters.journal.JournalWriter;
import xyz.zlatanov.frakkintoasters.journal.fact.DeckFact.CardsAddedOnTop;
import xyz.zlatanov.frakkintoasters.journal.fact.DeckFact.CardsDiscarded;
import xyz.zlatanov.frakkintoasters.journal.fact.DeckFact.CardsDrawn;
import xyz.zlatanov.frakkintoasters.journal.fact.DeckFact.DeckShuffled;
import xyz.zlatanov.frakkintoasters.journal.fact.Fact;
import xyz.zlatanov.frakkintoasters.journal.fact.ShipFact.ShipMoved;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.board.CylonFleetBoard;
import xyz.zlatanov.frakkintoasters.state.board.GalacticaBoard;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.card.MutinyCard;
import xyz.zlatanov.frakkintoasters.state.damage.BasestarDamage;
import xyz.zlatanov.frakkintoasters.state.damage.GalacticaDamage;
import xyz.zlatanov.frakkintoasters.state.damage.PegasusDamage;
import xyz.zlatanov.frakkintoasters.state.deck.Deck;
import xyz.zlatanov.frakkintoasters.state.deck.DeckId;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;
import xyz.zlatanov.frakkintoasters.state.ship.CylonShip;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.ArrayList;
import java.util.List;

import static xyz.zlatanov.frakkintoasters.state.deck.DeckId.*;
import static xyz.zlatanov.frakkintoasters.state.ship.ShipZone.CYLON_SUPPLY;
import static xyz.zlatanov.frakkintoasters.state.ship.ShipZone.GALACTICA_SPACE;

public abstract class AbstractOps {

    protected final Game          game;
    protected final JournalWriter journal;

    protected AbstractOps(Game game) {
        this.game = game;
        this.journal = game.journal();
    }

    /**
     * Applies the fact to the game and appends it to the journal. The only way ops change state: ops compute a fact
     * from the current state and record it, and nothing else writes to the components.
     */
    protected void record(Fact fact) {
        FactApplier.apply(game, fact);
        journal.record(fact);
    }

    /**
     * Combines the draw and discard piles of the deck and shuffles them. Logged as a {@code DeckShuffled} fact.
     */
    protected void shuffle(DeckId id) {
        Deck<Object> deck = game.decks().deck(id);
        List<Object> all = new ArrayList<>(deck.cards());
        all.addAll(deck.discardedCards());
        record(new DeckShuffled(id, game.randomness().shuffled(all)));
    }

    /**
     * Draws the top card. The card is in no pile afterwards, so the caller has to record where it went. When the draw
     * empties a deck that reshuffles its discard pile, the reshuffle is logged too.
     */
    protected <T> T draw(DeckId id) {
        Deck<T> deck = game.decks().deck(id);
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
     * Moves the next ship of the type from the Cylon supply to a Galactica space location, if the supply has one.
     */
    protected void takeFromSupply(Class<? extends CylonShip> type, Location location) {
        game.cylonShips().next(type)
                .ifPresent(ship -> record(new ShipMoved(ship.id(), CYLON_SUPPLY, GALACTICA_SPACE, location)));
    }

    protected GalacticaBoard galacticaBoard() {
        return game.boards().galactica();
    }

    protected CylonFleetBoard cylonFleetBoard() {
        return game.boards().cylonFleet();
    }
}
