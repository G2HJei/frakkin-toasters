package xyz.zlatanov.frakkintoasters.backend.game;

import org.springframework.stereotype.Component;
import xyz.zlatanov.frakkintoasters.state.Randomness;

/**
 * Hands out the randomness of a new game. A bean, so a test can seed it.
 */
@Component
public class RandomnessSource {

    public Randomness next() {
        return new Randomness();
    }
}
