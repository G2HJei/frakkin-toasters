package xyz.zlatanov.frakkintoasters.operation;

import xyz.zlatanov.frakkintoasters.journal.FactApplier;
import xyz.zlatanov.frakkintoasters.journal.JournalWriter;
import xyz.zlatanov.frakkintoasters.journal.fact.Fact;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.board.CylonFleetBoard;
import xyz.zlatanov.frakkintoasters.state.board.GalacticaBoard;

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

    protected GalacticaBoard galacticaBoard() {
        return game.boards().galactica();
    }

    protected CylonFleetBoard cylonFleetBoard() {
        return game.boards().cylonFleet();
    }
}
