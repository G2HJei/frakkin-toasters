package xyz.zlatanov.frakkintoasters.engine;

import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.state.Randomness;

public final class TestEngines {

    private TestEngines() {
    }

    public static GameEngine start(int playerCount, Randomness randomness, Event first) {
        return new GameEngine(playerCount, randomness, first);
    }

    /**
     * A seeded engine: its randomness comes from the seed and the size of the journal, so a restore with the seed goes
     * on like the original.
     */
    public static GameEngine start(int playerCount, long seed, Event first) {
        return new GameEngine(playerCount, seed, first);
    }
}
