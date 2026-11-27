package xyz.zlatanov.frakkintoasters.backend.game;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import xyz.zlatanov.frakkintoasters.backend.BackendTest;
import xyz.zlatanov.frakkintoasters.backend.game.CommandResult.Accepted;
import xyz.zlatanov.frakkintoasters.backend.journal.JournalCodec;
import xyz.zlatanov.frakkintoasters.engine.EngineStatus.Finished;
import xyz.zlatanov.frakkintoasters.engine.PendingInput.AwaitingChoice;
import xyz.zlatanov.frakkintoasters.engine.ScriptEvent;
import xyz.zlatanov.frakkintoasters.engine.TestEngines;
import xyz.zlatanov.frakkintoasters.engine.journal.JournalEntry.OnlyAnswerTaken;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.endgame.CylonsWinEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.projection.LiveUpdateProjection.ScopeLog;
import xyz.zlatanov.frakkintoasters.projection.Viewer;
import xyz.zlatanov.frakkintoasters.state.Randomness;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.Team.CYLONS;

class OnlyAnswersTest extends BackendTest {

    private static final Followup PASS_OR_PASS_TWICE = one(single(new NoOpEvent(1)),
            all(new NoOpEvent(1), new NoOpEvent(1)));
    private static final Followup PASS               = single(new PlayerDecisionEvent<>(1, NoOpEvent.class));

    @Autowired
    GameSessions sessions;
    @Autowired
    GameBundles  bundles;

    @Test
    void aDecisionWithOneAnswerRunsWithoutItsPlayerAlsoAfterARestore() {
        val game = bundles.create("only answers", createUser("host"), JournalCodec.FORMAT_VERSION);
        sessions.start(game, TestEngines.start(3, Randomness.seeded(1), new ScriptEvent(all(
                PASS_OR_PASS_TWICE, PASS, PASS_OR_PASS_TWICE, PASS, single(new CylonsWinEvent())))));

        assertInstanceOf(Accepted.class, choose(game));

        assertInstanceOf(AwaitingChoice.class, sessions.cached(game).orElseThrow().engine().pendingInput().orElseThrow());
        assertEquals(1, onlyAnswers(game));
        assertEquals(1, automaticScopes(game, Viewer.player(1)));
        assertEquals(0, automaticScopes(game, Viewer.player(2)));

        sessions.evict(game);
        assertInstanceOf(Accepted.class, choose(game));

        assertEquals(2, onlyAnswers(game));
        assertEquals(new Finished(CYLONS), sessions.cached(game).orElseThrow().engine().status(1));
    }

    private CommandResult choose(UUID game) {
        val viewer = Viewer.player(1);
        return sessions.choose(game, viewer, sessions.status(game, viewer).token(), 0);
    }

    private long onlyAnswers(UUID game) {
        return sessions.journal(game).entries().stream().filter(OnlyAnswerTaken.class::isInstance).count();
    }

    private long automaticScopes(UUID game, Viewer viewer) {
        return sessions.history(game, viewer).log().entries().stream()
                .filter(entry -> entry instanceof ScopeLog scope && scope.automatic())
                .count();
    }
}
