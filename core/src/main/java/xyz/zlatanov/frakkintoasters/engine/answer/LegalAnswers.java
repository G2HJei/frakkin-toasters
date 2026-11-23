package xyz.zlatanov.frakkintoasters.engine.answer;

import lombok.val;
import xyz.zlatanov.frakkintoasters.event.AnytimeEvent;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.state.view.GameView;

import java.util.List;
import java.util.Optional;

/**
 * The legal answers to a decision, for the player it asks. Every candidate comes from the game as it is and is judged
 * by its processor's {@code isValid()}, as a client's input is, so no rule is written twice (see
 * {@code legal-answers-plan.md}).
 * <ul>
 *     <li>a concrete answer is an event {@code submit} takes as it is, with the decision's constraints on it</li>
 *     <li>a shaped answer has a part to pick, e.g. the cards of a discard: its example is legal, and its pick says what
 *     the part may hold. Where a rule reads the picked cards themselves (a strength, one Reckless card), a filling of
 *     the pick may still be rejected</li>
 * </ul>
 * Every legal answer is one of them: {@link #covers(Event)}.
 *
 * @param events the concrete answers
 * @param shapes the answers with a part to pick
 */
public record LegalAnswers(List<Event> events, List<ShapedAnswer> shapes) {

    public static final LegalAnswers NONE = new LegalAnswers(List.of(), List.of());

    /**
     * What the search couldn't judge. Nothing of it reaches the table: the fuzzer reports it.
     */
    public interface Listener {

        Listener IGNORE = new Listener() {
            @Override
            public void crashed(Class<?> type, Event candidate, RuntimeException thrown) {
            }

            @Override
            public void unsupported(Class<?> type, String component, String why) {
            }
        };

        /**
         * A check threw instead of rejecting a candidate, which a client could send too.
         *
         * @param candidate {@code null} when the cheap check of the class threw
         */
        void crashed(Class<?> type, Event candidate, RuntimeException thrown);

        /**
         * The search has no values to try for a component of a class that may answer, so that class is left out.
         */
        void unsupported(Class<?> type, String component, String why);
    }

    public LegalAnswers {
        events = List.copyOf(events);
        shapes = List.copyOf(shapes);
    }

    public static LegalAnswers of(GameView game, PlayerDecisionEvent<?> decision) {
        return of(game, decision, Listener.IGNORE);
    }

    public static LegalAnswers of(GameView game, PlayerDecisionEvent<?> decision, Listener listener) {
        return new AnswerSearch(new Asked(game, decision), listener).run();
    }

    /**
     * @return the {@link AnytimeEvent}s the player may submit now without being asked, e.g. a Motive Card whose
     * requirement is met. They answer no decision: the engine runs one ahead of the pending input
     */
    public static LegalAnswers anytime(GameView game, int playerNumber) {
        return of(game, new PlayerDecisionEvent<>(playerNumber, AnytimeEvent.class));
    }

    /**
     * @return the answers of the decision the option asks first, {@link #NONE} for an option that asks none at once.
     * Choosing an option changes nothing before its decision is asked, so they are the answers it will have.
     */
    public static LegalAnswers forOption(GameView game, Followup option) {
        val decision = decisionOf(option);
        return decision == null ? NONE : of(game, decision);
    }

    /**
     * @return the decision an option asks first: the option itself, or the first of an {@code all(...)}; {@code null}
     * when it asks none at once
     */
    public static PlayerDecisionEvent<?> decisionOf(Followup option) {
        return switch (option) {
            case Followup.Single(PlayerDecisionEvent<?> decision) -> decision;
            case Followup.AllOf allOf when !allOf.followups().isEmpty() -> decisionOf(allOf.followups().getFirst());
            case null, default -> null;
        };
    }

    /**
     * @return the event classes that may answer a decision for the action: the action itself, or every record of the
     * rules that implements it
     */
    public static List<Class<? extends Event>> answerTypes(Class<?> action) {
        return AnswerClasses.of(action).stream().<Class<? extends Event>>map(AnswerClasses.AnswerClass::type).toList();
    }

    public boolean isEmpty() {
        return events.isEmpty() && shapes.isEmpty();
    }

    /**
     * @return the answer when there is no other: a single concrete answer, or a single shape that takes every element
     * of its pick, in any order
     */
    public Optional<Event> only() {
        if (events.size() == 1 && shapes.isEmpty()) {
            return Optional.of(events.getFirst());
        }
        if (events.isEmpty() && shapes.size() == 1 && hasOneFilling(shapes.getFirst().pick())) {
            // the example of a shape is its smallest size, here every element
            return Optional.of(shapes.getFirst().example());
        }
        return Optional.empty();
    }

    // By position, as the player picks: two equal cards in his hand are two ways to discard one, so an answer taken for
    // him tells nobody that they are equal.
    private static boolean hasOneFilling(Pick pick) {
        val count = pick.elements().size();
        return pick.min() == count && pick.max() == count && (!pick.ordered() || count <= 1);
    }

    /**
     * @param event an answer as {@code submit} takes it, with the decision's constraints
     * @return whether it is one of the concrete answers, or fits a shape. A list compares as a multiset.
     */
    public boolean covers(Event event) {
        return event != null && (events.stream().anyMatch(answer -> AnswerMatch.same(answer, event))
                || shapes.stream().anyMatch(shape -> AnswerMatch.fits(shape, event)));
    }
}
