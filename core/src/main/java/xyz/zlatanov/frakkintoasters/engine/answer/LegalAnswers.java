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
 * Every candidate is judged by its processor's {@code isValid()}, as a client's input is, so no rule is written twice.
 * Where a rule reads the picked cards themselves (a strength, one Reckless card), a filling of a shaped answer's pick may
 * still be rejected.
 */
public record LegalAnswers(List<Event> events, List<ShapedAnswer> shapes) {

    public static final LegalAnswers NONE = new LegalAnswers(List.of(), List.of());

    /**
     * Nothing of it reaches the table: the fuzzer reports it.
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

        void crashed(Class<?> type, Event candidate, RuntimeException thrown);

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

    public static LegalAnswers anytime(GameView game, int playerNumber) {
        return of(game, new PlayerDecisionEvent<>(playerNumber, AnytimeEvent.class));
    }

    /**
     * Choosing an option changes nothing before its decision is asked, so they are the answers it will have.
     */
    public static LegalAnswers forOption(GameView game, Followup option) {
        val decision = decisionOf(option);
        return decision == null ? NONE : of(game, decision);
    }

    public static PlayerDecisionEvent<?> decisionOf(Followup option) {
        return switch (option) {
            case Followup.Single(PlayerDecisionEvent<?> decision) -> decision;
            case Followup.AllOf allOf when !allOf.followups().isEmpty() -> decisionOf(allOf.followups().getFirst());
            case null, default -> null;
        };
    }

    public static List<Class<? extends Event>> answerTypes(Class<?> action) {
        return AnswerClasses.of(action).stream().<Class<? extends Event>>map(AnswerClasses.AnswerClass::type).toList();
    }

    public boolean isEmpty() {
        return events.isEmpty() && shapes.isEmpty();
    }

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

    public boolean covers(Event event) {
        return event != null && (events.stream().anyMatch(answer -> AnswerMatch.same(answer, event))
                || shapes.stream().anyMatch(shape -> AnswerMatch.fits(shape, event)));
    }
}
