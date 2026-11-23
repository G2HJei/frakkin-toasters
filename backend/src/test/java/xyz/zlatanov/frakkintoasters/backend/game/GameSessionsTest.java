package xyz.zlatanov.frakkintoasters.backend.game;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import xyz.zlatanov.frakkintoasters.backend.BackendTest;
import xyz.zlatanov.frakkintoasters.backend.game.CommandResult.Accepted;
import xyz.zlatanov.frakkintoasters.backend.game.CommandResult.Reason;
import xyz.zlatanov.frakkintoasters.backend.game.CommandResult.Rejected;
import xyz.zlatanov.frakkintoasters.backend.game.GameBundle.GameStatus;
import xyz.zlatanov.frakkintoasters.backend.journal.JournalCodec;
import xyz.zlatanov.frakkintoasters.engine.AnytimeTestEvent;
import xyz.zlatanov.frakkintoasters.engine.FailingEvent;
import xyz.zlatanov.frakkintoasters.engine.GameEngine;
import xyz.zlatanov.frakkintoasters.engine.ScriptEvent;
import xyz.zlatanov.frakkintoasters.engine.ScriptedTurns;
import xyz.zlatanov.frakkintoasters.engine.TestEngines;
import xyz.zlatanov.frakkintoasters.engine.journal.GameSnapshot;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.endgame.CylonsWinEvent;
import xyz.zlatanov.frakkintoasters.projection.GameProjection.Choose;
import xyz.zlatanov.frakkintoasters.projection.Viewer;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.Randomness;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;

// The scripted turns answer every pause, so the sessions ask for every answer, as the engine in memory does.
@TestPropertySource(properties = "frakkin.take-only-answers=false")
class GameSessionsTest extends BackendTest {

    @Autowired
    GameSessions sessions;
    @Autowired
    GameBundles  bundles;
    @Autowired
    JournalCodec codec;

    private UUID game;

    @BeforeEach
    void createGame() {
        game = bundles.create("test", createUser("host"), JournalCodec.FORMAT_VERSION);
    }

    @Test
    void aGameRestoredFromTheRowsBeforeEveryCommandPlaysLikeOneInMemory() {
        val inMemory = ScriptedTurns.start();
        sessions.start(game, ScriptedTurns.start());
        assertSameGame(inMemory);

        for (val next : ScriptedTurns.inputs()) {
            sessions.evict(game);
            val input = next.apply(inMemory.view());
            input.applyTo(inMemory);

            assertInstanceOf(Accepted.class, send(input));
            assertSameGame(inMemory);
        }
        sessions.evict(game);
        assertEquals(GameSnapshot.of((Game) inMemory.view()), snapshot());
        val bundle = bundles.find(game).orElseThrow();
        assertEquals(List.of(GameStatus.RUNNING, 5), List.of(bundle.status(), bundle.turn()));
        assertEquals(inMemory.pendingInput().orElseThrow().player(), bundle.waitingForPlayerNumber());
    }

    @Test
    void ofTwoAnswersToTheSameInputOneIsStale() throws Exception {
        val inMemory = ScriptedTurns.start();
        sessions.start(game, ScriptedTurns.start());
        val input = ScriptedTurns.inputs().getFirst().apply(inMemory.view());
        val token = sessions.status(game, Viewer.player(input.player())).token();
        val json = codec.writeEvent(input.submit());

        val start = new CountDownLatch(1);
        val results = new ArrayList<CommandResult>();
        try (val threads = Executors.newFixedThreadPool(2)) {
            val futures = List.of(
                    threads.submit(() -> {
                        start.await();
                        return sessions.submit(game, Viewer.player(input.player()), token, json);
                    }),
                    threads.submit(() -> {
                        start.await();
                        return sessions.submit(game, Viewer.player(input.player()), token, json);
                    }));
            start.countDown();
            for (val future : futures) {
                results.add(future.get());
            }
        }

        assertEquals(1, results.stream().filter(Accepted.class::isInstance).count(), results.toString());
        val stale = (Rejected) results.stream().filter(Rejected.class::isInstance).findFirst().orElseThrow();
        assertEquals(Reason.STALE, stale.reason());
        assertNotNull(stale.game()); // the sender gets the current game
    }

    @Test
    void aFailedAppendEvictsTheSessionAndTheNextCommandSeesTheStoredGame() {
        val inMemory = ScriptedTurns.start();
        sessions.start(game, ScriptedTurns.start());
        val before = snapshot();
        val sequence = rows();
        // a second writer was faster
        jdbc.sql("INSERT INTO journal_entry (game_bundle_id, sequence, entry) VALUES (:game, :sequence, '{}'::jsonb)")
                .param("game", game)
                .param("sequence", sequence + 1)
                .update();
        val input = ScriptedTurns.inputs().getFirst().apply(inMemory.view());

        val result = (Rejected) send(input);

        assertEquals(Reason.FAILURE, result.reason());
        assertTrue(sessions.cached(game).isEmpty());
        jdbc.sql("DELETE FROM journal_entry WHERE game_bundle_id = :game AND sequence > :sequence")
                .param("game", game)
                .param("sequence", sequence)
                .update();
        assertEquals(before, snapshot());
        assertInstanceOf(Accepted.class, send(input));
    }

    @Test
    void anInputThatFailsInTheEngineRollsBackAndWritesNothing() {
        val failing = single(new FailingEvent());
        sessions.start(game, TestEngines.start(3, Randomness.seeded(1),
                new ScriptEvent(all(one(single(new NoOpEvent(1)), failing), single(new CylonsWinEvent())))));
        val session = sessions.cached(game).orElseThrow();
        val sequence = rows();
        val choose = (Choose) sessions.status(game, Viewer.player(1)).game().input();

        val result = (Rejected) sessions.choose(game, Viewer.player(1), sessions.status(game, Viewer.player(1)).token(),
                choose.options().indexOf(failing));

        assertEquals(Reason.FAILURE, result.reason());
        assertEquals(sequence, rows());
        assertSame(session, sessions.cached(game).orElseThrow());
        assertEquals(sequence, session.sequence());
    }

    // Player 2 acts while player 1 is asked: the choice stays pending, and the event's player is the connection's.
    @Test
    void anAnytimeEventRunsAheadOfAnotherPlayersInput() {
        val choice = one(single(new NoOpEvent(1)), single(new CylonsWinEvent()));
        sessions.start(game, TestEngines.start(3, Randomness.seeded(1),
                new ScriptEvent(all(choice, single(new CylonsWinEvent())))));
        val sequence = rows();
        val json = (tools.jackson.databind.node.ObjectNode) codec.writeEvent(new AnytimeTestEvent(3));

        val accepted = sessions.submit(game, Viewer.player(2), sessions.status(game, Viewer.player(2)).token(), json);

        assertInstanceOf(Accepted.class, accepted);
        assertTrue(rows() > sequence);
        sessions.evict(game);
        assertFalse(liveGame().player(2).hasMiracleToken());
        assertTrue(liveGame().player(3).hasMiracleToken());
        assertInstanceOf(Choose.class, sessions.status(game, Viewer.player(1)).game().input());
        // he has no miracle token left to spend
        val again = sessions.submit(game, Viewer.player(2), sessions.status(game, Viewer.player(2)).token(), json);
        assertEquals(Reason.INVALID, ((Rejected) again).reason());
    }

    // In a sandbox the user answers for everyone, so the event names the player who acts.
    @Test
    void aSandboxSubmitsAnAnytimeEventForThePlayerItNames() {
        sessions.start(game, TestEngines.start(3, Randomness.seeded(1),
                new ScriptEvent(all(one(single(new NoOpEvent(1)), single(new CylonsWinEvent())), single(new CylonsWinEvent())))));
        val token = sessions.status(game, Viewer.sandbox()).token();

        assertInstanceOf(Accepted.class, sessions.submit(game, Viewer.sandbox(), token, codec.writeEvent(new AnytimeTestEvent(3))));

        assertFalse(liveGame().player(3).hasMiracleToken());
        assertTrue(liveGame().player(1).hasMiracleToken());
    }

    @Test
    void aGameOfAnotherFormatVersionIsNotOpened() {
        val old = bundles.create("old", createUser("host"), JournalCodec.FORMAT_VERSION + 1);
        bundles.updateSummary(old, GameStatus.RUNNING, 3, null, 1);

        val failure = assertThrows(GameNotAvailableException.class, () -> sessions.status(old, Viewer.observer()));

        assertTrue(failure.getMessage().contains("another version"), failure.getMessage());
    }

    @Test
    void anObserverCantPlayAndADecisionTakesOnlyItsType() {
        val inMemory = ScriptedTurns.start();
        sessions.start(game, ScriptedTurns.start());
        val input = ScriptedTurns.inputs().getFirst().apply(inMemory.view());
        val token = sessions.status(game, Viewer.observer()).token();

        assertEquals(Reason.INVALID, ((Rejected) sessions.submit(game, Viewer.observer(), token,
                codec.writeEvent(input.submit()))).reason());
        assertEquals(Reason.INVALID, ((Rejected) sessions.submit(game, Viewer.player(input.player()), token,
                codec.writeEvent(new NoOpEvent(input.player())))).reason());
        // the player number comes from the connection, not from the client
        val json = (tools.jackson.databind.node.ObjectNode) codec.writeEvent(input.submit());
        json.put("playerNumber", input.player() % 3 + 1);
        assertInstanceOf(Accepted.class, sessions.submit(game, Viewer.player(input.player()), token, json));
    }

    /* helpers */

    private CommandResult send(ScriptedTurns.Input input) {
        val viewer = Viewer.player(input.player());
        val status = sessions.status(game, viewer);
        if (input.submit() != null) {
            return sessions.submit(game, viewer, status.token(), codec.writeEvent(input.submit()));
        }
        val options = ((Choose) status.game().input()).options();
        return sessions.choose(game, viewer, status.token(), options.indexOf(input.choose()));
    }

    private void assertSameGame(GameEngine inMemory) {
        val expected = GameSnapshot.of((Game) inMemory.view()).values();
        val actual = snapshot().values();
        val differing = expected.keySet().stream()
                .filter(key -> !java.util.Objects.equals(expected.get(key), actual.get(key)))
                .map(key -> key + ": " + expected.get(key) + " != " + actual.get(key))
                .toList();
        assertEquals(List.of(), differing);
    }

    private Game liveGame() {
        sessions.status(game, Viewer.observer()); // loads the session
        return (Game) sessions.cached(game).orElseThrow().view();
    }

    private GameSnapshot snapshot() {
        sessions.status(game, Viewer.observer()); // loads the session
        return GameSnapshot.of((Game) sessions.cached(game).orElseThrow().view());
    }

    private int rows() {
        return jdbc.sql("SELECT count(*) FROM journal_entry WHERE game_bundle_id = :game")
                .param("game", game)
                .query(Integer.class)
                .single();
    }
}
