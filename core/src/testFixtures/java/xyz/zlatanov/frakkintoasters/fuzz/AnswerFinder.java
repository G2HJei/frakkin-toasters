package xyz.zlatanov.frakkintoasters.fuzz;

import lombok.val;
import xyz.zlatanov.frakkintoasters.engine.ProcessorRegistry;
import xyz.zlatanov.frakkintoasters.engine.answer.LegalAnswers;
import xyz.zlatanov.frakkintoasters.engine.answer.ShapedAnswer;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.event.constraint.ConstrainedEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.state.exception.InvalidActionException;
import xyz.zlatanov.frakkintoasters.state.view.GameView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.stream.IntStream;

/**
 * Finds a random legal answer to a decision, from the game's own {@link LegalAnswers}. The pick is uniform over the
 * event classes that can answer, then over the answers of that class; a shape is filled at random and judged again,
 * since a rule that reads the picked cards may reject a filling, and its example is the answer when none fits.
 */
public final class AnswerFinder {

    private static final int FILLINGS = 8;

    private AnswerFinder() {
    }

    /**
     * @return a legal answer, as {@code submit} would take it, or empty when the decision has no legal answer
     */
    public static Optional<Event> find(GameView game, PlayerDecisionEvent<?> decision, Random random,
                                       Consumer<Finding> findings) {
        return find(game, decision, answers(game, decision, findings), random, findings);
    }

    public static Optional<Event> find(GameView game, PlayerDecisionEvent<?> decision, LegalAnswers answers,
                                       Random random, Consumer<Finding> findings) {
        val byClass = new TreeMap<String, List<Object>>(); // a class's events and shapes, in name order
        answers.events().forEach(event -> byClass.computeIfAbsent(event.getClass().getName(), key -> new ArrayList<>())
                .add(event));
        answers.shapes().forEach(shape -> byClass.computeIfAbsent(shape.example().getClass().getName(),
                key -> new ArrayList<>()).add(shape));
        if (byClass.isEmpty()) {
            return Optional.empty();
        }
        val ofClass = new ArrayList<>(byClass.values()).get(random.nextInt(byClass.size()));
        val choice = ofClass.get(random.nextInt(ofClass.size()));
        if (choice instanceof Event event) {
            return Optional.of(event);
        }
        val shape = (ShapedAnswer) choice;
        for (int i = 0; i < FILLINGS; i++) {
            val filled = fill(shape, random);
            if (filled != null) {
                val legal = legal(game, decision, filled, findings);
                if (legal.isPresent()) {
                    return legal;
                }
            }
        }
        return Optional.of(shape.example());
    }

    /**
     * @return the legal answers, with what their search couldn't judge reported as findings
     */
    public static LegalAnswers answers(GameView game, PlayerDecisionEvent<?> decision, Consumer<Finding> findings) {
        try {
            return LegalAnswers.of(game, decision, new LegalAnswers.Listener() {
                @Override
                public void crashed(Class<?> type, Event candidate, RuntimeException thrown) {
                    val finding = Finding.of(Finding.Kind.INPUT_CRASH, thrown, type.getSimpleName());
                    findings.accept(candidate == null ? finding : finding.withEvent(candidate));
                }

                @Override
                public void unsupported(Class<?> type, String component, String why) {
                    val name = type.getSimpleName() + (component.isEmpty() ? "" : "." + component);
                    findings.accept(Finding.of(Finding.Kind.FUZZER, "no answers for " + name,
                            "The legal answers leave out `" + name + "`: " + why));
                }
            });
        } catch (Throwable thrown) {
            findings.accept(Finding.of(Finding.Kind.FUZZER, thrown, "LegalAnswers"));
            return LegalAnswers.NONE;
        }
    }

    /**
     * @return the candidate with the decision's constraints, as {@code submit} takes it, when it is a legal answer
     */
    public static Optional<Event> legal(GameView game, PlayerDecisionEvent<?> decision, Event candidate,
                                        Consumer<Finding> findings) {
        val name = candidate.getClass().getSimpleName();
        final Event event;
        try {
            event = constrained(decision, candidate);
        } catch (Throwable thrown) {
            findings.accept(Finding.of(Finding.Kind.INPUT_CRASH, thrown, name).withEvent(candidate));
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
            findings.accept(Finding.of(Finding.Kind.INPUT_CRASH, thrown, name).withEvent(candidate));
            return Optional.empty();
        }
    }

    static Event constrained(PlayerDecisionEvent<?> decision, Event candidate) {
        return candidate instanceof ConstrainedEvent constrained
                ? constrained.withConstraints(decision.eventConstraints())
                : candidate;
    }

    // A random filling of the pick: a size between its bounds, random elements by position, in a random order if it
    // counts.
    static Event fill(ShapedAnswer shape, Random random) {
        val pick = shape.pick();
        val type = shape.example().getClass();
        val index = Records.indexOf(type, pick.component());
        val component = type.getRecordComponents()[index];
        val elements = pick.elements();
        if (elements.isEmpty()) {
            return null;
        }
        if (!Collection.class.isAssignableFrom(component.getType())) {
            return Records.with(shape.example(), index, elements.get(random.nextInt(elements.size())));
        }
        val max = Math.min(pick.max(), elements.size());
        val size = pick.min() + random.nextInt(Math.max(1, max - pick.min() + 1));
        val positions = new ArrayList<>(IntStream.range(0, elements.size()).boxed().toList());
        Collections.shuffle(positions, random);
        val chosen = positions.subList(0, Math.min(size, positions.size()));
        if (!pick.ordered()) {
            Collections.sort(chosen);
        }
        List<Object> picked = chosen.stream().map(position -> (Object) elements.get(position)).toList();
        Object value = component.getType() == Set.class ? new LinkedHashSet<>(picked) : picked;
        return Records.with(shape.example(), index, value);
    }
}
