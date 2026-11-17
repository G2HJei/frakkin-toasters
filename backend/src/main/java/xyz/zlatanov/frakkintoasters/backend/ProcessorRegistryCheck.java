package xyz.zlatanov.frakkintoasters.backend;

import lombok.val;
import xyz.zlatanov.frakkintoasters.engine.GameEngine;
import xyz.zlatanov.frakkintoasters.state.Randomness;

/**
 * Starts a game inside the packaged application, where {@code core} is a nested jar, to check that the processor
 * registry finds the processors there. {@code PackagedJarTest} runs it with
 * {@code java -cp backend.jar -Dloader.main=<this class> org.springframework.boot.loader.launch.PropertiesLauncher}.
 */
public final class ProcessorRegistryCheck {

    private ProcessorRegistryCheck() {
    }

    public static void main(String[] args) {
        val engine = GameEngine.start(3, Randomness.seeded(1));
        System.out.println("status " + engine.status(1));
    }
}
