package xyz.zlatanov.frakkintoasters.operation;

import xyz.zlatanov.frakkintoasters.journal.FactApplier;
import xyz.zlatanov.frakkintoasters.journal.JournalWriter;
import xyz.zlatanov.frakkintoasters.journal.fact.DeckFact.DeckShuffled;
import xyz.zlatanov.frakkintoasters.journal.fact.Fact;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.board.CylonFleetBoard;
import xyz.zlatanov.frakkintoasters.state.board.GalacticaBoard;
import xyz.zlatanov.frakkintoasters.state.deck.Deck;
import xyz.zlatanov.frakkintoasters.state.deck.DeckId;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractOps {

    protected final Game          game;
    protected final JournalWriter journal;

    protected AbstractOps(Game game) {
        this.game = game;
        this.journal = game.journal();
    }

    /**
     * Applies the fact to the game and appends it to the journal. The only way ops should change state.
     */
    protected void record(Fact fact) {
        FactApplier.apply(game, fact);
        journal.record(fact);
    }

    protected void shuffle(DeckId id) {
        Deck<Object> deck = game.decks().deck(id);
        List<Object> all = new ArrayList<>(deck.cards());
        all.addAll(deck.discardedCards());
        record(new DeckShuffled(id, game.randomness().shuffled(all)));
    }

    protected <T> T draw(DeckId id) {
        Deck<T> deck = game.decks().deck(id);
        T card = deck.draw();
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

    protected GalacticaBoard galacticaBoard() {
        return game.boards().galactica();
    }

    protected CylonFleetBoard cylonFleetBoard() {
        return game.boards().cylonFleet();
    }
}
