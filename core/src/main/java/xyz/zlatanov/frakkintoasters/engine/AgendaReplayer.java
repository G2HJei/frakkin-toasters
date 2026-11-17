package xyz.zlatanov.frakkintoasters.engine;

import lombok.NoArgsConstructor;
import lombok.val;
import xyz.zlatanov.frakkintoasters.engine.journal.Journal;
import xyz.zlatanov.frakkintoasters.engine.journal.JournalEntry.ChoiceMade;
import xyz.zlatanov.frakkintoasters.engine.journal.JournalEntry.EventInjected;
import xyz.zlatanov.frakkintoasters.engine.journal.JournalEntry.FactRecorded;
import xyz.zlatanov.frakkintoasters.engine.journal.JournalEntry.ScopeClosed;
import xyz.zlatanov.frakkintoasters.engine.journal.JournalEntry.ScopeOpened;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.GameFact.GameEnded;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup.OneOf;
import xyz.zlatanov.frakkintoasters.event.Followup.Single;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;

import java.util.ArrayList;
import java.util.List;

import static lombok.AccessLevel.PRIVATE;
import static xyz.zlatanov.frakkintoasters.engine.journal.Journal.ROOT_SCOPE;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;

/**
 * Rebuilds the agenda of a game from its journal, with the moves the run loop made. Of the facts only the end of the
 * game matters here, {@link xyz.zlatanov.frakkintoasters.engine.journal.GameReplayer} applies them.
 */
@NoArgsConstructor(access = PRIVATE)
public final class AgendaReplayer {

    /**
     * @param inputStarts the index of the first entry of each input, in order: the scope of an event that answered a
     *                    decision, a choice, and an injected event
     */
    public record Replay(Agenda agenda, List<Integer> inputStarts) {
    }

    private final Agenda        agenda      = new Agenda();
    private final List<Integer> inputStarts = new ArrayList<>();
    private       int           openScope   = ROOT_SCOPE;
    private       int           lastScope   = ROOT_SCOPE;
    private       boolean       ended;

    /**
     * @return the agenda at the end of the journal
     * @throws IllegalArgumentException if the journal doesn't match the moves of a run loop, or ends neither at a pause
     *                                  nor with the end of the game
     */
    public static Agenda replay(Journal journal) {
        return replayWithInputs(journal).agenda();
    }

    /**
     * @return the agenda at the end of the journal, and where each input began
     * @throws IllegalArgumentException as {@link #replay(Journal)}
     */
    public static Replay replayWithInputs(Journal journal) {
        val replayer = new AgendaReplayer();
        val entries = journal.entries();
        for (int index = 0; index < entries.size(); index++) {
            switch (entries.get(index)) {
                case ScopeOpened opened -> replayer.open(opened, index);
                case FactRecorded(int ignoredScope, GameEnded ignoredFact) -> replayer.ended = true;
                case FactRecorded ignored -> {
                }
                case ScopeClosed closed -> replayer.close(closed);
                case ChoiceMade choice -> replayer.select(choice, index);
                case EventInjected injected -> replayer.inject(injected, index);
            }
        }
        replayer.requirePaused();
        return new Replay(replayer.agenda, List.copyOf(replayer.inputStarts));
    }

    private void open(ScopeOpened opened, int index) {
        require(openScope == ROOT_SCOPE, "Scope " + opened.scopeId() + " opens inside scope " + openScope);
        require(!ended, opened + " runs after the game ended");
        if (lastScope == ROOT_SCOPE) {
            require(opened.parentId() == ROOT_SCOPE, "The first event has a parent: " + opened);
        } else {
            val frame = agenda.top().orElseThrow(() -> new IllegalArgumentException("Nothing was pending for " + opened));
            require(frame.parentScope() == opened.parentId() && runs(frame, opened.event()), opened + " doesn't run " + frame);
            if (frame.followup() instanceof Single(PlayerDecisionEvent<?> ignored)) {
                inputStarts.add(index); // a player answered the decision
            }
            agenda.take();
        }
        openScope = lastScope = opened.scopeId();
    }

    // A decision runs the event that answered it, any other frame its own event.
    private static boolean runs(Agenda.Frame frame, Event event) {
        return switch (frame.followup()) {
            case Single(PlayerDecisionEvent<?> decision) -> decision.action().isInstance(event);
            case Single(Event queued) -> queued.equals(event);
            default -> false;
        };
    }

    private void close(ScopeClosed closed) {
        require(closed.scopeId() == openScope, "Scope " + closed.scopeId() + " closes, but " + openScope + " is open");
        agenda.push(closed.followup(), closed.scopeId());
        if (ended) {
            agenda.clear(); // as the run loop does after the event that ended the game
        }
        openScope = ROOT_SCOPE;
    }

    private void select(ChoiceMade choice, int index) {
        require(openScope == ROOT_SCOPE, choice + " is made inside scope " + openScope);
        val frame = agenda.top().orElse(null);
        require(frame != null
                        && frame.followup() instanceof OneOf oneOf
                        && frame.parentScope() == choice.scopeId()
                        && oneOf.options().contains(choice.option()),
                choice + " doesn't answer " + frame);
        inputStarts.add(index);
        agenda.select(choice.option());
    }

    // The engine pushes an injected event on top of the pending input, under the root.
    private void inject(EventInjected injected, int index) {
        require(openScope == ROOT_SCOPE, injected + " is injected inside scope " + openScope);
        require(!ended, injected + " is injected after the game ended");
        require(lastScope != ROOT_SCOPE, injected + " is injected before the game started");
        inputStarts.add(index);
        agenda.push(single(injected.event()), ROOT_SCOPE);
    }

    private void requirePaused() {
        require(openScope == ROOT_SCOPE, "The journal ends inside scope " + openScope);
        require(ended || !agenda.isEmpty(), "Nothing is left to run, but the game goes on"); // as in the run loop
        agenda.top()
                .filter(frame -> frame.followup() instanceof Single(Event event)
                        && !(event instanceof PlayerDecisionEvent<?>))
                .ifPresent(frame -> {
                    throw new IllegalArgumentException("The journal ends before " + frame + " ran");
                });
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }
}
