package xyz.zlatanov.frakkintoasters.journal;

import xyz.zlatanov.frakkintoasters.journal.fact.Fact;

/**
 * The narrow view of the journal that game ops get: they can only append facts.
 */
public interface JournalWriter {

    void record(Fact fact);
}
