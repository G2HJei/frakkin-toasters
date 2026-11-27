package xyz.zlatanov.frakkintoasters.backend.game;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import xyz.zlatanov.frakkintoasters.backend.BackendTest;
import xyz.zlatanov.frakkintoasters.backend.game.CommandResult.Rejected;
import xyz.zlatanov.frakkintoasters.backend.game.GameBundle.EndReason;
import xyz.zlatanov.frakkintoasters.backend.game.GameBundle.GameStatus;
import xyz.zlatanov.frakkintoasters.backend.journal.JournalCodec;
import xyz.zlatanov.frakkintoasters.backend.lobby.Lobby;
import xyz.zlatanov.frakkintoasters.backend.lobby.Lobby.Role;
import xyz.zlatanov.frakkintoasters.backend.lobby.Participants.Participant;
import xyz.zlatanov.frakkintoasters.backend.lobby.Seats;
import xyz.zlatanov.frakkintoasters.backend.lobby.Seats.LeaveReason;
import xyz.zlatanov.frakkintoasters.backend.lobby.Seats.SeatHolder;
import xyz.zlatanov.frakkintoasters.backend.lobby.Seats.VoteKind;
import xyz.zlatanov.frakkintoasters.engine.GameEngine;
import xyz.zlatanov.frakkintoasters.engine.PendingInput.AwaitingChoice;
import xyz.zlatanov.frakkintoasters.engine.PendingInput.AwaitingDecision;
import xyz.zlatanov.frakkintoasters.fuzz.AnswerFinder;
import xyz.zlatanov.frakkintoasters.projection.Viewer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The exit of milestone D: a game of four played over "three days", restored from the rows twice a day, whose second
 * seat is handed over mid-game by a vote of the others to an observer who goes on in it, until the game ends with the
 * engine's winner, the results' report and the seat's history.
 */
class SessionExitTest extends BackendTest {

    private static final long SEED       = 11L;
    private static final int  INPUT_CAP  = 20_000;
    // the game "restarts" this often: the session is dropped, and the next command restores it from the rows
    private static final int  RESTART_AT = 40;

    @Autowired
    Lobby        lobby;
    @Autowired
    Seats        seats;
    @Autowired
    GameBundles  bundles;
    @Autowired
    GameSessions sessions;
    @Autowired
    JournalCodec codec;

    @Test
    void aGameOverDaysWithAPlayerReplacedMidGameFinishesWithItsResults() {
        val names = List.of("Adama", "Roslin", "Starbuck", "Apollo");
        val users = new ArrayList<UUID>();
        names.forEach(name -> users.add(createUser(name)));
        val dualla = createUser("Dualla");
        val room = lobby.host(users.getFirst(), "Three days");
        users.subList(1, 4).forEach(user -> lobby.join(room.id(), user, Role.PLAYER));
        lobby.join(room.id(), dualla, Role.OBSERVER);
        val game = room.id();
        // what the lobby's start does, with a seeded engine, so the game is the same every run
        for (int seat = 1; seat <= 4; seat++) {
            seats.take(game, seat, users.get(seat - 1));
        }
        bundles.playerCount(game, 4);
        sessions.start(game, GameEngine.start(4, SEED));

        val random = new Random(SEED);
        var replaced = false;
        var restarts = 0;
        for (int input = 1; input <= INPUT_CAP; input++) {
            if (bundles.find(game).orElseThrow().status() == GameStatus.FINISHED) {
                break;
            }
            if (input % RESTART_AT == 0) {
                sessions.evict(game);
                restarts++;
            }
            if (!replaced && input == 150) {
                lobby.vote(game, users.get(2), VoteKind.VACATE, 2);
                val voted = lobby.vote(game, users.get(3), VoteKind.VACATE, 2);
                assertEquals(List.of(2), voted.vacantSeats());
                val taken = lobby.takeSeat(game, dualla, 2);
                assertEquals(dualla, holder(taken.players(), 2));
                replaced = true;
            }
            answer(game, random);
        }

        val bundle = bundles.find(game).orElseThrow();
        assertEquals(GameStatus.FINISHED, bundle.status(), "the game ends");
        assertEquals(EndReason.WON, bundle.endReason());
        assertTrue(replaced, "the seat was handed over mid-game");
        assertTrue(restarts >= 4);
        val session = sessions.cached(game).orElseThrow();
        val outcome = session.view().outcome().orElseThrow();
        assertEquals(outcome, bundle.winnerTeam());
        val report = session.report().orElseThrow();
        assertEquals(outcome, report.winner());
        assertEquals(session.view().winners(), report.winners());
        sessions.evict(game);
        assertEquals(outcome, sessions.history(game, Viewer.observer()).report().winner());
        val second = lobby.room(game).seatHistory().stream().filter(holder -> holder.playerNumber() == 2).toList();
        assertEquals(List.of(users.get(1), dualla), second.stream().map(SeatHolder::userId).toList());
        assertEquals(LeaveReason.VOTED_OUT, second.getFirst().reason());
    }

    private void answer(UUID game, Random random) {
        val status = sessions.status(game, Viewer.observer());
        val engine = sessions.cached(game).orElseThrow().engine();
        val result = switch (engine.pendingInput().orElseThrow()) {
            case AwaitingDecision(var decision) -> sessions.submit(game, Viewer.player(decision.playerNumber()),
                    status.token(), codec.writeEvent(AnswerFinder.find(engine.view(), decision, random, finding -> {
                    }).orElseThrow()));
            case AwaitingChoice(int player, var options) ->
                    sessions.choose(game, Viewer.player(player), status.token(), random.nextInt(options.size()));
        };
        if (result instanceof Rejected rejected) {
            fail("refused: " + rejected.message());
        }
    }

    private static UUID holder(List<Participant> players, int seat) {
        return players.stream().filter(player -> player.playerNumber() == seat).findFirst().orElseThrow().userId();
    }
}
