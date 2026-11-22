package xyz.zlatanov.frakkintoasters.fuzz;

import lombok.val;
import xyz.zlatanov.frakkintoasters.engine.ProcessorRegistry;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.event.constraint.ConstrainedEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.fuzz.AnswerClasses.AnswerClass;
import xyz.zlatanov.frakkintoasters.state.exception.InvalidActionException;
import xyz.zlatanov.frakkintoasters.state.view.GameView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;
import java.util.Random;
import java.util.function.Consumer;

/**
 * Finds a random legal answer to a decision. It builds candidates of every class that can answer, and the processors
 * judge them: {@code prepare} only reads. The pick is uniform over the classes that have a legal answer, because the
 * classes are tried in random order and the first legal candidate is the answer.
 */
public final class AnswerFinder {

    private AnswerFinder() {
    }

    /**
     * @return a legal answer, as {@code submit} would take it, or empty when even every candidate of every class fails
     */
    public static Optional<Event> find(GameView game, PlayerDecisionEvent<?> decision, Random random,
                                       Consumer<Finding> findings) {
        val asked = new Asked(game, decision, random, findings);
        val classes = new ArrayList<>(AnswerClasses.of(decision.action()));
        Collections.shuffle(classes, random);
        for (val answerClass : classes) {
            if (answerClass.mayAnswer(asked)) {
                val answer = firstLegal(asked, answerClass, Candidates.NORMAL);
                if (answer.isPresent()) {
                    return answer;
                }
            }
        }
        // Before saying there is none: every candidate of every class, whatever the game's checks say.
        for (val answerClass : classes) {
            val answer = firstLegal(asked, answerClass, Candidates.EXHAUSTIVE);
            if (answer.isPresent()) {
                return answer;
            }
        }
        return Optional.empty();
    }

    /**
     * @return the candidate with the decision's constraints, as {@code submit} takes it, when it is a legal answer
     */
    public static Optional<Event> legal(GameView game, PlayerDecisionEvent<?> decision, Event candidate,
                                        Consumer<Finding> findings) {
        val name = candidate.getClass().getSimpleName();
        final Event event;
        try {
            event = candidate instanceof ConstrainedEvent constrained
                    ? constrained.withConstraints(decision.eventConstraints())
                    : candidate;
        } catch (Throwable thrown) {
            findings.accept(Finding.of(Finding.Kind.INPUT_CRASH, thrown, name));
            return Optional.empty();
        }
        if (!decision.action().isInstance(event)
                || event instanceof PlayerEvent playerEvent && playerEvent.playerNumber() != decision.playerNumber()) {
            return Optional.empty();
        }
        try {
            ProcessorRegistry.create(event).prepare(game, event);
            return Optional.of(event);
        } catch (InvalidActionException rejected) {
            return Optional.empty();
        } catch (Throwable thrown) {
            // A client may send this, and the game should reject it, not throw.
            findings.accept(Finding.of(Finding.Kind.INPUT_CRASH, thrown, name));
            return Optional.empty();
        }
    }

    private static Optional<Event> firstLegal(Asked asked, AnswerClass answerClass, Candidates.Budget budget) {
        final java.util.List<? extends Event> candidates;
        try {
            candidates = Candidates.of(asked, answerClass, budget);
        } catch (Throwable thrown) {
            asked.findings().accept(Finding.of(Finding.Kind.FUZZER, thrown, answerClass.type().getSimpleName()));
            return Optional.empty();
        }
        for (val candidate : candidates) {
            val answer = legal(asked.game(), asked.decision(), candidate, asked.findings());
            if (answer.isPresent()) {
                return answer;
            }
        }
        return Optional.empty();
    }
}
