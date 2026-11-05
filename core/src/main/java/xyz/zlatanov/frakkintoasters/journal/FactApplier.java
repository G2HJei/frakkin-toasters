package xyz.zlatanov.frakkintoasters.journal;

import xyz.zlatanov.frakkintoasters.journal.fact.Fact;
import xyz.zlatanov.frakkintoasters.journal.fact.GameFact;
import xyz.zlatanov.frakkintoasters.journal.fact.TitleFact;
import xyz.zlatanov.frakkintoasters.state.Game;

/**
 * The only place that calls component write methods on behalf of a fact. Used for live play (through
 * {@code AbstractOps.record}) and for replay, so both share one code path.
 */
public final class FactApplier {

    private FactApplier() {
    }

    public static void apply(Game game, Fact fact) {
        switch (fact) {
            case GameFact f -> apply(game, f);
            case TitleFact f -> apply(game, f);
        }
    }

    private static void apply(Game game, GameFact fact) {
        switch (fact) {
            case GameFact.StepChanged f -> game.step(f.step());
            case GameFact.TurnStarted f -> game.turn(f.turn());
            case GameFact.CurrentPlayerChanged f -> game.currentPlayerNumber(f.playerNumber());
            case GameFact.ActingPlayerChanged f -> game.actingPlayerNumber(f.playerNumber());
        }
    }

    private static void apply(Game game, TitleFact fact) {
        switch (fact) {
            case TitleFact.PresidentAssigned f -> game.president(f.character());
            case TitleFact.AdmiralAssigned f -> game.admiral(f.character());
            case TitleFact.CagAssigned f -> game.cag(f.character());
        }
    }
}
