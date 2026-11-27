package xyz.zlatanov.frakkintoasters.engine.journal;

import xyz.zlatanov.frakkintoasters.engine.journal.fact.Fact;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;

/**
 * A flat list of entries, so the journal is easy to serialize.
 */
public sealed interface JournalEntry {

    int scopeId();

    record ScopeOpened(int scopeId, int parentId, Event event, int actor) implements JournalEntry {
    }

    record FactRecorded(int scopeId, Fact fact) implements JournalEntry {
    }

    record ScopeClosed(int scopeId, Followup followup) implements JournalEntry {
    }

    record EventInjected(Event event) implements JournalEntry {

        @Override
        public int scopeId() {
            return Journal.ROOT_SCOPE;
        }
    }

    record ChoiceMade(int scopeId, int chooser, Followup option) implements JournalEntry {
    }

    record OnlyAnswerTaken(int scopeId) implements JournalEntry {
    }
}
