package xyz.zlatanov.frakkintoasters.state;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * The single source of randomness of a game. Seedable, so a game can be made deterministic. Replay doesn't need it,
 * because every random result is recorded in a fact.
 */
public class Randomness {

    private final Random random;

    public Randomness() {
        this(new Random());
    }

    private Randomness(Random random) {
        this.random = random;
    }

    public static Randomness seeded(long seed) {
        return new Randomness(new Random(seed));
    }

    public void reseed(long seed) {
        random.setSeed(seed);
    }

    public int nextInt(int origin, int bound) {
        return random.nextInt(origin, bound);
    }

    public <T> List<T> shuffled(Collection<? extends T> elements) {
        List<T> result = new ArrayList<>(elements);
        Collections.shuffle(result, random);
        return result;
    }
}
