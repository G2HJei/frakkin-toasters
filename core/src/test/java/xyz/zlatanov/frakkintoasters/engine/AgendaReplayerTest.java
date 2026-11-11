package xyz.zlatanov.frakkintoasters.engine;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.engine.Agenda.Frame;
import xyz.zlatanov.frakkintoasters.engine.journal.Journal;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.GameFact.GameEnded;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.GameFact.TurnStarted;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.SelectCharacterEvent;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.engine.journal.Journal.ROOT_SCOPE;
import static xyz.zlatanov.frakkintoasters.event.Followup.*;
import static xyz.zlatanov.frakkintoasters.state.Team.CYLONS;

class AgendaReplayerTest {

    private final Journal journal = new Journal();

    @Test
    void replayMakesTheMovesOfTheRunLoop() {
        val decision = single(new PlayerDecisionEvent<>(1, NoOpEvent.class));
        val choice = one(event(3), decision);
        val first = journal.openScope(new NoOpEvent(1), 1, ROOT_SCOPE); // pushed before the journal started
        journal.record(new TurnStarted(1));
        journal.closeScope(all(event(2), choice));
        journal.openScope(new NoOpEvent(2), 2, first);
        journal.closeScope(NONE);
        assertEquals(Optional.of(new Frame(choice, first)), AgendaReplayer.replay(journal).top());

        journal.recordChoice(first, 1, decision);
        assertEquals(Optional.of(new Frame(decision, first)), AgendaReplayer.replay(journal).top());

        journal.openScope(new NoOpEvent(1), 1, first); // the answer to the decision, which ends the game
        journal.record(new GameEnded(CYLONS));
        journal.closeScope(NONE);
        assertTrue(AgendaReplayer.replay(journal).isEmpty());
    }

    @Test
    void aJournalCantRunOutOfEventsBeforeTheGameEnds() {
        journal.openScope(new NoOpEvent(1), 1, ROOT_SCOPE);
        journal.closeScope(NONE);
        assertDoesntFit();
    }

    @Test
    void theFirstEventHasNoParent() {
        journal.openScope(new NoOpEvent(1), 1, 5);
        journal.closeScope(NONE);
        assertDoesntFit();
    }

    @Test
    void nothingRunsAfterTheAgendaIsEmpty() {
        journal.openScope(new NoOpEvent(1), 1, ROOT_SCOPE);
        journal.closeScope(NONE);
        journal.openScope(new NoOpEvent(2), 1, ROOT_SCOPE);
        journal.closeScope(NONE);
        assertDoesntFit();
    }

    @Test
    void onlyTheEventOnTopRuns() {
        val first = journal.openScope(new NoOpEvent(1), 1, ROOT_SCOPE);
        journal.closeScope(all(event(2), event(3)));
        journal.openScope(new NoOpEvent(3), 1, first);
        journal.closeScope(NONE);
        assertDoesntFit();
    }

    @Test
    void anEventRunsUnderTheScopeThatQueuedIt() {
        journal.openScope(new NoOpEvent(1), 1, ROOT_SCOPE);
        journal.closeScope(event(2));
        journal.openScope(new NoOpEvent(2), 1, ROOT_SCOPE);
        journal.closeScope(NONE);
        assertDoesntFit();
    }

    @Test
    void anAnswerHasToFitTheDecision() {
        val first = journal.openScope(new NoOpEvent(1), 1, ROOT_SCOPE);
        journal.closeScope(single(new PlayerDecisionEvent<>(1, SelectCharacterEvent.class)));
        journal.openScope(new NoOpEvent(1), 1, first);
        journal.closeScope(NONE);
        assertDoesntFit();
    }

    @Test
    void onlyAnOptionOfTheChoiceOnTopCanBeChosen() {
        val first = journal.openScope(new NoOpEvent(1), 1, ROOT_SCOPE);
        journal.closeScope(one(event(2), event(3)));
        journal.recordChoice(first, 1, event(4));
        assertDoesntFit();
    }

    @Test
    void aJournalHasToEndAtAPause() {
        journal.openScope(new NoOpEvent(1), 1, ROOT_SCOPE);
        journal.closeScope(event(2));
        assertDoesntFit();
    }

    @Test
    void theEndOfTheGameClearsTheAgenda() {
        endTheGameWithEvent3Pending();
        assertTrue(AgendaReplayer.replay(journal).isEmpty());
    }

    @Test
    void nothingRunsAfterTheGameEnded() {
        endTheGameWithEvent3Pending();
        journal.openScope(new NoOpEvent(3), 1, ROOT_SCOPE);
        journal.closeScope(NONE);
        assertDoesntFit();
    }

    @Test
    void aJournalCantEndInsideAScope() {
        journal.openScope(new NoOpEvent(1), 1, ROOT_SCOPE);
        assertDoesntFit();
    }

    private void endTheGameWithEvent3Pending() {
        val first = journal.openScope(new NoOpEvent(1), 1, ROOT_SCOPE);
        journal.closeScope(all(event(2), event(3)));
        journal.openScope(new NoOpEvent(2), 1, first);
        journal.record(new GameEnded(CYLONS));
        journal.closeScope(NONE);
    }

    private void assertDoesntFit() {
        assertThrows(IllegalArgumentException.class, () -> AgendaReplayer.replay(journal));
    }

    private static Followup event(int n) {
        return single(new NoOpEvent(n));
    }
}
