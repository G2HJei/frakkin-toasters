package xyz.zlatanov.frakkintoasters.backend.lobby;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.server.ResponseStatusException;
import xyz.zlatanov.frakkintoasters.backend.BackendTest;
import xyz.zlatanov.frakkintoasters.backend.game.GameBundle.GameStatus;
import xyz.zlatanov.frakkintoasters.backend.lobby.Lobby.Role;
import xyz.zlatanov.frakkintoasters.backend.lobby.Participants.Participant;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;

class LobbyTest extends BackendTest {

    @Autowired
    Lobby lobby;

    @Test
    void theHostTakesTheFirstSeatAndOthersTheNextFreeOne() {
        val host = createUser("Adama");
        val room = lobby.host(host, "Galactica");

        assertEquals(GameStatus.LOBBY, room.status());
        assertEquals(List.of(1), seats(room));
        val roslin = createUser("Roslin");
        assertEquals(List.of(1, 2), seats(lobby.join(room.id(), roslin, Role.PLAYER)));
        assertEquals(List.of(1, 2, 3), seats(lobby.join(room.id(), createUser("Starbuck"), Role.PLAYER)));
        lobby.leave(room.id(), roslin);
        assertEquals(List.of(1, 2, 3), seats(lobby.join(room.id(), createUser("Apollo"), Role.PLAYER)));
    }

    @Test
    void aUserIsInAGameOnceAndCanSwitchBeforeTheStart() {
        val host = createUser("Adama");
        val room = lobby.host(host, "Galactica");
        val tigh = createUser("Tigh");
        lobby.join(room.id(), tigh, Role.PLAYER);

        assertConflict(() -> lobby.join(room.id(), tigh, Role.PLAYER));
        val switched = lobby.join(room.id(), tigh, Role.OBSERVER);
        assertEquals(List.of(1), seats(switched));
        assertEquals(List.of(tigh), switched.observers().stream().map(Participant::userId).toList());
        assertEquals(List.of(1, 2), seats(lobby.join(room.id(), tigh, Role.PLAYER)));
        assertConflict(() -> lobby.leave(room.id(), host)); // the host stays
    }

    @Test
    void theHostStartsWith3To7PlayersAndTheSeatsCloseUp() {
        val host = createUser("Adama");
        val room = lobby.host(host, "Galactica");
        val roslin = createUser("Roslin");
        lobby.join(room.id(), roslin, Role.PLAYER);
        assertConflict(() -> lobby.start(room.id(), host)); // 2 players
        val starbuck = createUser("Starbuck");
        lobby.join(room.id(), starbuck, Role.PLAYER);
        val apollo = createUser("Apollo");
        lobby.join(room.id(), apollo, Role.PLAYER);
        lobby.leave(room.id(), roslin); // seats 1, 3 and 4 are left
        assertConflict(() -> lobby.start(room.id(), starbuck)); // not the host

        val started = lobby.start(room.id(), host);

        assertEquals(GameStatus.RUNNING, started.status());
        assertEquals(List.of(1, 2, 3), seats(started));
        assertEquals(List.of(host, starbuck, apollo), started.players().stream().map(Participant::userId).toList());
        // after the start, only observers join
        assertConflict(() -> lobby.join(room.id(), createUser("Gaeta"), Role.PLAYER));
        assertEquals(1, lobby.join(room.id(), createUser("Dualla"), Role.OBSERVER).observers().size());
        // a player who leaves now leaves his seat, see SeatsTest
        assertEquals(List.of(3), lobby.leave(room.id(), apollo).vacantSeats());
    }

    @Test
    void twoUsersRaceForTheLastSeat() throws Exception {
        val room = lobby.host(createUser("Adama"), "Galactica");
        for (int i = 2; i <= 6; i++) {
            lobby.join(room.id(), createUser("Pilot " + i), Role.PLAYER);
        }
        val racers = List.of(createUser("Kat"), createUser("Hotdog"));

        val start = new CountDownLatch(1);
        val outcomes = new ArrayList<String>();
        try (val threads = Executors.newFixedThreadPool(2)) {
            val futures = new ArrayList<Future<String>>();
            for (val racer : racers) {
                futures.add(threads.submit(() -> {
                    start.await();
                    try {
                        lobby.join(room.id(), racer, Role.PLAYER);
                        return "seated";
                    } catch (ResponseStatusException e) {
                        return "refused";
                    }
                }));
            }
            start.countDown();
            for (val future : futures) {
                outcomes.add(future.get());
            }
        }

        assertEquals(List.of("refused", "seated"), outcomes.stream().sorted().toList());
        assertEquals(List.of(1, 2, 3, 4, 5, 6, 7), seats(lobby.room(room.id())));
    }

    @Test
    void theListShowsOpenLobbiesMyGamesAndRunningGames() {
        val me = createUser("Gaeta");
        val mine = lobby.host(me, "Mine");
        val open = lobby.host(createUser("Zarek"), "Open");
        val running = lobby.host(createUser("Cain"), "Running");
        lobby.join(running.id(), createUser("Fisk"), Role.PLAYER);
        lobby.join(running.id(), createUser("Shaw"), Role.PLAYER);
        lobby.start(running.id(), running.hostUserId());

        val games = lobby.games(me);

        assertTrue(ids(games.mine()).contains(mine.id()));
        assertTrue(ids(games.open()).contains(open.id()));
        assertFalse(ids(games.open()).contains(mine.id()));
        assertTrue(ids(games.running()).contains(running.id()));
        assertFalse(ids(games.open()).contains(running.id()));
    }

    private static List<Integer> seats(Lobby.Room room) {
        return room.players().stream().map(Participant::playerNumber).toList();
    }

    private static List<UUID> ids(List<Lobby.GameSummary> games) {
        return games.stream().map(Lobby.GameSummary::id).toList();
    }

    private static void assertConflict(Runnable call) {
        assertThrows(ResponseStatusException.class, call::run);
    }
}
