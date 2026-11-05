package xyz.zlatanov.frakkintoasters.journal;

import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.journal.fact.Fact;

/**
 * One line of the journal. The journal is a flat list of entries, so it is easy to serialize later.
 */
public sealed interface JournalEntry {

    int scopeId();

    /**
     * An event started being processed.
     *
     * @param parentId the scope of the event that queued this one, {@link Journal#ROOT_SCOPE} if none
     * @param actor    the number of the acting player
     */
    record ScopeOpened(int scopeId, int parentId, Event event, int actor) implements JournalEntry {
    }

    record FactRecorded(int scopeId, Fact fact) implements JournalEntry {
    }

    record ScopeClosed(int scopeId) implements JournalEntry {
    }
}
