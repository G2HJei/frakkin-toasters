package xyz.zlatanov.frakkintoasters.projection;

import xyz.zlatanov.frakkintoasters.engine.journal.JournalEntry;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.Fact;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.PlayerPile;
import xyz.zlatanov.frakkintoasters.state.deck.DeckId;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCheckPile;

import java.util.List;

/**
 * A function of the journal and the viewer alone, so it can be served again after a reconnect. See
 * {@link LiveUpdateProjector} for the rules.
 */
public record LiveUpdateProjection(List<LogEntry> entries) {

    /**
     * @param journal the whole journal, which the rules need, e.g. who was President when a Quorum Card was drawn
     */
    public static LiveUpdateProjection of(List<JournalEntry> journal, int from, Viewer viewer) {
        return new LiveUpdateProjector(journal, viewer).project(from);
    }

    public static LiveUpdateProjection of(List<JournalEntry> journal, Viewer viewer) {
        return of(journal, 0, viewer);
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }

    public sealed interface LogEntry {
    }

    /**
     * @param details   {@code null} when the fields aren't public: everyone sees the name and the actor, and the facts
     *                  tell what happened
     * @param automatic the engine answered the actor's decision with it, his only answer. Only he sees it: that it was
     *                  his only one can tell what he holds
     */
    public record ScopeLog(int scopeId, int parentId, int actor, Class<? extends Event> event, Event details,
                           List<FactProjection> facts, boolean automatic) implements LogEntry {
    }

    public record ChoiceLog(int scopeId, int chooser, Followup option) implements LogEntry {
    }

    public sealed interface FactProjection {
    }

    public record Shown(Fact fact, List<GameProjection.ShownCards> revealed) implements FactProjection {

        public Shown(Fact fact) {
            this(fact, null);
        }
    }

    /**
     * A face down card doesn't show even the color of a Skill Card.
     */
    public record Hidden(Class<? extends Fact> fact,
                         Integer playerNumber,
                         Integer otherPlayerNumber,
                         DeckId deck,
                         PlayerPile pile,
                         SkillCheckPile skillCheckPile,
                         int count) implements FactProjection {
    }
}
