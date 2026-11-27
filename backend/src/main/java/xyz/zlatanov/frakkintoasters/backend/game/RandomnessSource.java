package xyz.zlatanov.frakkintoasters.backend.game;

import org.springframework.stereotype.Component;
import xyz.zlatanov.frakkintoasters.state.Randomness;

/**
 * A bean, so a test can seed it.
 */
@Component
public class RandomnessSource {

    public Randomness next() {
        return new Randomness();
    }
}
