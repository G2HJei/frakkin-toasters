package xyz.zlatanov.frakkintoasters.engine.journal;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.engine.journal.JournalEntry.FactRecorded;
import xyz.zlatanov.frakkintoasters.engine.journal.JournalEntry.ScopeClosed;
import xyz.zlatanov.frakkintoasters.engine.journal.JournalEntry.ScopeOpened;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.GameFact.TurnStarted;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JournalTest {
    Journal journal = new Journal();

    @Test
    void factsOutsideAScopeGoToTheRoot() {
        journal.record(new TurnStarted(1));
        assertEquals(List.of(new FactRecorded(Journal.ROOT_SCOPE, new TurnStarted(1))), journal.entries());
    }

    @Test
    void scopesNestAndFactsBelongToTheInnermostOne() {
        val outer = journal.openScope(null, 1);
        journal.record(new TurnStarted(1));
        val inner = journal.openScope(null, 2);
        journal.record(new TurnStarted(2));
        journal.closeScope();
        journal.record(new TurnStarted(3));
        journal.closeScope();

        assertEquals(List.of(new TurnStarted(1), new TurnStarted(3)), journal.facts(outer));
        assertEquals(List.of(new TurnStarted(2)), journal.facts(inner));
        assertEquals(3, journal.facts().size());
        assertEquals(new ScopeOpened(inner, outer, null, 2), journal.entries().get(2));
        assertEquals(new ScopeClosed(inner), journal.entries().get(4));
        assertEquals(Journal.ROOT_SCOPE, journal.currentScope());
    }

    @Test
    void closingWithoutAnOpenScopeIsAProgrammingError() {
        assertThrows(FrakCallTheAdmiralException.class, journal::closeScope);
    }
}
