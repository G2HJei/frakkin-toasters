package xyz.zlatanov.frakkintoasters.engine.journal;

import xyz.zlatanov.frakkintoasters.engine.journal.fact.Fact;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;

/**
 * One line of the journal. The journal is a flat list of entries, so it is easy to serialize later.
 */
public sealed interface JournalEntry {

    int scopeId();

    /**
     * An event started being processed.
     *
     * @param parentId the scope of the event that queued this one, {@link Journal#ROOT_SCOPE} if none
     * @param actor    the number of the player the event is logged under, see
     *                 {@link xyz.zlatanov.frakkintoasters.engine.ScopeActor}
     */
    record ScopeOpened(int scopeId, int parentId, Event event, int actor) implements JournalEntry {
    }

    record FactRecorded(int scopeId, Fact fact) implements JournalEntry {
    }

    /**
     * An event finished being processed.
     *
     * @param followup what the event queued
     */
    record ScopeClosed(int scopeId, Followup followup) implements JournalEntry {
    }

    /**
     * A player picked an option of a {@link Followup.OneOf}.
     *
     * @param scopeId the scope of the event that queued the {@code OneOf}
     * @param option  the picked option
     */
    record ChoiceMade(int scopeId, int chooser, Followup option) implements JournalEntry {
    }
}
