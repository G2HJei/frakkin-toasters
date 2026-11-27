package xyz.zlatanov.frakkintoasters.fuzz;

import lombok.val;
import xyz.zlatanov.frakkintoasters.engine.GameEngine;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.fuzz.Finding.Kind;
import xyz.zlatanov.frakkintoasters.state.exception.InvalidActionException;

/**
 * Submits an input a client could send but the agent never would, and judges what the engine did with it. Rejected, it
 * leaves the game as it was; taken, the game goes on with it; anything else is a finding.
 */
public final class Probe {

    public record Result(boolean taken, Finding finding) {
    }

    private Probe() {
    }

    public static Result submit(GameEngine engine, int player, Event event) {
        val name = event.getClass().getSimpleName();
        val before = Twins.State.of(engine);
        val journal = engine.journal();
        try {
            engine.submit(player, event);
            return new Result(true, null);
        } catch (InvalidActionException rejected) {
            return new Result(false, changed(engine, before, name, event));
        } catch (Throwable thrown) {
            // A rollback replaces the journal, and an Error past it leaves its scope behind: either way it was taken.
            if (engine.journal() != journal || engine.journal().size() != before.journalSize()) {
                return new Result(false, Finding.of(Kind.CRASH, thrown, name).withEvent(event));
            }
            // The check threw before anything ran.
            val changed = changed(engine, before, name, event);
            return new Result(false, changed != null ? changed
                    : Finding.of(Kind.INPUT_CRASH, thrown, name).withEvent(event));
        }
    }

    private static Finding changed(GameEngine engine, Twins.State before, String name, Event event) {
        val differences = Twins.differences(before, Twins.State.of(engine));
        if (differences.isEmpty()) {
            return null;
        }
        return Finding.of(Kind.REJECTED_BUT_CHANGED, "rejected " + name + " changed the game",
                        "A rejected `" + name + "` changed the game")
                .withDetail(String.join("\n", differences))
                .withEvent(event);
    }
}
