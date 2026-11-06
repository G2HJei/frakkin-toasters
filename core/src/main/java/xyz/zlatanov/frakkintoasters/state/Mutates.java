package xyz.zlatanov.frakkintoasters.state;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method of a state component that changes the game state. Replay only works when every change is a recorded
 * fact, so only {@code journal.FactApplier} (and the state package itself) may call these methods. Game ops record a
 * fact, and the applier calls the component. An ArchUnit rule ({@code ArchitectureTest}) checks it.
 * <p>
 * It can't cover the mutable collections that some getters expose (e.g. {@code Board.characters()}), so don't change
 * state through those.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Mutates {
}
