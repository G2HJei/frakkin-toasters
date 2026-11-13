package xyz.zlatanov.frakkintoasters.state;

import java.lang.annotation.*;

/**
 * Marks a method of a state component that changes the game state. Replay only works when every change is a recorded
 * fact, so only {@code engine.journal.FactApplier} (and the state package itself) may call these methods. Game ops
 * record a fact, and the applier calls the component. An ArchUnit rule ({@code ArchitectureTest}) checks it.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Mutates {
}
