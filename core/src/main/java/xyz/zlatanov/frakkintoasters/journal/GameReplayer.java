package xyz.zlatanov.frakkintoasters.journal;

import lombok.val;
import xyz.zlatanov.frakkintoasters.journal.fact.GameFact.GameStarted;
import xyz.zlatanov.frakkintoasters.state.Die;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.Randomness;

public final class GameReplayer {

    private GameReplayer() {
    }

    public static Game replay(Journal journal) {
        return replay(journal, new Randomness());
    }

    /**
     * @param randomness the randomness the replayed game gets. It has no effect on the result
     */
    public static Game replay(Journal journal, Randomness randomness) {
        val facts = journal.facts();
        if (facts.isEmpty() || !(facts.getFirst() instanceof GameStarted started)) {
            throw new IllegalArgumentException("A journal has to start with GameStarted");
        }
        val game = Game.builder(started.playerCount())
                .objective(started.objective())
                .randomness(randomness)
                .die(new Die(randomness))
                .build();
        facts.stream().skip(1).forEach(fact -> FactApplier.apply(game, fact));
        return game;
    }
}
