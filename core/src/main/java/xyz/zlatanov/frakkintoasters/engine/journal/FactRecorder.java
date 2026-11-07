package xyz.zlatanov.frakkintoasters.engine.journal;

import lombok.RequiredArgsConstructor;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.Fact;
import xyz.zlatanov.frakkintoasters.state.Game;

/**
 * The only way for changing the game state (outside of tests).
 */
@RequiredArgsConstructor
public class FactRecorder {

    private final Game    game;
    private final Journal journal;

    public void record(Fact fact) {
        FactApplier.apply(game, fact);
        journal.record(fact);
    }
}
