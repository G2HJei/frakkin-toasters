package xyz.zlatanov.frakkintoasters.engine.journal;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.engine.journal.JournalEntry.ChoiceMade;
import xyz.zlatanov.frakkintoasters.engine.journal.JournalEntry.FactRecorded;
import xyz.zlatanov.frakkintoasters.engine.journal.JournalEntry.ScopeClosed;
import xyz.zlatanov.frakkintoasters.engine.journal.JournalEntry.ScopeOpened;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.GameFact.TurnStarted;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static xyz.zlatanov.frakkintoasters.engine.journal.Journal.ROOT_SCOPE;
import static xyz.zlatanov.frakkintoasters.event.Followup.*;

class JournalTest {
    Journal journal = new Journal();

    @Test
    void factsOutsideAScopeGoToTheRoot() {
        journal.record(new TurnStarted(1));
        assertEquals(List.of(new FactRecorded(ROOT_SCOPE, new TurnStarted(1))), journal.entries());
    }

    @Test
    void aFollowupScopeNamesItsParentAfterTheParentClosed() {
        val event = new NoOpEvent(1);
        val followup = single(new NoOpEvent(2));

        val parent = journal.openScope(event, 1, ROOT_SCOPE);
        journal.record(new TurnStarted(1));
        journal.closeScope(followup);
        val child = journal.openScope(new NoOpEvent(2), 2, parent);
        journal.record(new TurnStarted(2));
        journal.closeScope(NONE);
        journal.record(new TurnStarted(3));

        assertEquals(List.of(
                new ScopeOpened(parent, ROOT_SCOPE, event, 1),
                new FactRecorded(parent, new TurnStarted(1)),
                new ScopeClosed(parent, followup),
                new ScopeOpened(child, parent, new NoOpEvent(2), 2),
                new FactRecorded(child, new TurnStarted(2)),
                new ScopeClosed(child, NONE),
                new FactRecorded(ROOT_SCOPE, new TurnStarted(3))
        ), journal.entries());
        assertEquals(List.of(new TurnStarted(1)), journal.facts(parent));
        assertEquals(List.of(new TurnStarted(2)), journal.facts(child));
        assertEquals(ROOT_SCOPE, journal.currentScope());
    }

    @Test
    void onlyOneScopeIsOpenAtATime() {
        val scope = journal.openScope(null, 1, ROOT_SCOPE);
        assertThrows(FrakCallTheAdmiralException.class, () -> journal.openScope(null, 1, scope));
    }

    @Test
    void closingWithoutAnOpenScopeIsAProgrammingError() {
        assertThrows(FrakCallTheAdmiralException.class, () -> journal.closeScope(NONE));
    }

    @Test
    void aChoiceIsRecordedUnderTheScopeThatQueuedIt() {
        val option = single(new NoOpEvent(1));
        val parent = journal.openScope(null, 1, ROOT_SCOPE);
        journal.closeScope(one(option, single(new NoOpEvent(2))));

        journal.recordChoice(parent, 3, option);

        assertEquals(new ChoiceMade(parent, 3, option), journal.entries().getLast());
    }

    @Test
    void aChoiceIsMadeBetweenEvents() {
        val scope = journal.openScope(null, 1, ROOT_SCOPE);
        assertThrows(FrakCallTheAdmiralException.class, () -> journal.recordChoice(scope, 1, NONE));
    }
}
