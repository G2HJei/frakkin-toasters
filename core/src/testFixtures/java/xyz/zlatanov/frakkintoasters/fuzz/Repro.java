package xyz.zlatanov.frakkintoasters.fuzz;

import lombok.val;
import xyz.zlatanov.frakkintoasters.engine.GameEngine;
import xyz.zlatanov.frakkintoasters.engine.PendingInput.AwaitingChoice;
import xyz.zlatanov.frakkintoasters.engine.PendingInput.AwaitingDecision;
import xyz.zlatanov.frakkintoasters.engine.journal.Journal;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.state.exception.InvalidActionException;

import java.util.Arrays;
import java.util.Random;

/**
 * A finding as a regression journal keeps it: the journal at the pause before it, and the input that failed there.
 *
 * @param seed   the seed the game was played with, which a restore needs to go on as it did
 * @param player the player who gives the input
 * @param option the option he picks first, {@code null} for none
 * @param event  the event he submits then, {@code null} for none
 * @param expect what the input has to do once the bug is fixed, {@code null} when the journal only has to restore
 */
public record Repro(Journal journal, long seed, int player, Followup option, Event event, Expectation expect) {

    public enum Expectation {
        // applying the input throws nothing but InvalidActionException, and a rejected input changes nothing
        RUNS,
        // the asked player has a legal answer: to the pending decision, or to the option's decision (or it is gone)
        ANSWERS,
        // after the input, the journal restores to the same game
        RESTORES;

        public String json() {
            return name().toLowerCase();
        }

        public static Expectation ofJson(String json) {
            return Arrays.stream(values())
                    .filter(expectation -> expectation.json().equals(json))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("No expectation " + json));
        }
    }

    public static Repro journalOnly(Journal journal, long seed) {
        return new Repro(journal, seed, 0, null, null, null);
    }

    public boolean hasInput() {
        return option != null || event != null;
    }

    public GameEngine restore() {
        return GameEngine.restore(journal, seed);
    }

    public void applyTo(GameEngine engine) {
        if (option != null) {
            engine.choose(player, option);
        }
        if (event != null) {
            engine.submit(player, event);
        }
    }

    /**
     * Restores the journal and checks that the input does what {@link #expect} says.
     *
     * @throws AssertionError when it doesn't: the bug is still there
     */
    public void check() {
        val engine = restore();
        switch (expect) {
            case RUNS -> runs(engine);
            case ANSWERS -> answers(engine);
            case RESTORES -> restores(engine);
            case null -> {
            }
        }
    }

    private void runs(GameEngine engine) {
        if (option != null && !taken(engine, () -> engine.choose(player, option))) {
            return;
        }
        if (event != null) {
            taken(engine, () -> engine.submit(player, event));
        }
    }

    // Whether the engine took the input. One it rejects has to leave the game as it was.
    private static boolean taken(GameEngine engine, Runnable input) {
        val before = Twins.State.of(engine);
        try {
            input.run();
            return true;
        } catch (InvalidActionException rejected) {
            val differences = Twins.differences(before, Twins.State.of(engine));
            if (!differences.isEmpty()) {
                throw new AssertionError("A rejected input changed the game:\n" + String.join("\n", differences));
            }
            return false;
        } catch (Throwable thrown) {
            throw new AssertionError("The input threw " + thrown, thrown);
        }
    }

    private void answers(GameEngine engine) {
        val pending = engine.pendingInput().orElseThrow(() -> new AssertionError("The game is over"));
        if (option != null) {
            // an option that is no longer offered can't lead to a dead end
            if (pending instanceof AwaitingChoice choice && choice.options().contains(option)) {
                requireAnswer(engine, Playout.firstDecision(option));
            }
            return;
        }
        switch (pending) {
            case AwaitingDecision decision -> requireAnswer(engine, decision.decision());
            case AwaitingChoice choice -> {
                val live = choice.options().stream().anyMatch(o -> hasAnswer(engine, Playout.firstDecision(o)));
                if (!live) {
                    throw new AssertionError("Every option of " + choice + " leads to a decision without an answer");
                }
            }
        }
    }

    private static void requireAnswer(GameEngine engine, PlayerDecisionEvent<?> decision) {
        if (!hasAnswer(engine, decision)) {
            throw new AssertionError("No legal answer to " + decision);
        }
    }

    private static boolean hasAnswer(GameEngine engine, PlayerDecisionEvent<?> decision) {
        return decision == null || AnswerFinder.find(engine.view(), decision, new Random(0), finding -> {
        }).isPresent();
    }

    private void restores(GameEngine engine) {
        try {
            applyTo(engine);
        } catch (Throwable thrown) {
            throw new AssertionError("The input threw " + thrown, thrown);
        }
        val differences = Twins.differences(engine, GameEngine.restore(engine.journal(), seed));
        if (!differences.isEmpty()) {
            throw new AssertionError("The game restores differently after the input:\n" + String.join("\n", differences));
        }
    }
}
