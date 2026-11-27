package xyz.zlatanov.frakkintoasters.backend.lobby;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.server.ResponseStatusException;
import xyz.zlatanov.frakkintoasters.backend.BackendTest;
import xyz.zlatanov.frakkintoasters.backend.game.CommandResult.Reason;
import xyz.zlatanov.frakkintoasters.backend.game.CommandResult.Rejected;
import xyz.zlatanov.frakkintoasters.backend.game.GameBundle.EndReason;
import xyz.zlatanov.frakkintoasters.backend.game.GameBundle.GameStatus;
import xyz.zlatanov.frakkintoasters.backend.game.GameSessions;
import xyz.zlatanov.frakkintoasters.backend.lobby.Lobby.Role;
import xyz.zlatanov.frakkintoasters.backend.lobby.Lobby.Room;
import xyz.zlatanov.frakkintoasters.backend.lobby.Participants.Participant;
import xyz.zlatanov.frakkintoasters.backend.lobby.Seats.LeaveReason;
import xyz.zlatanov.frakkintoasters.backend.lobby.Seats.SeatHolder;
import xyz.zlatanov.frakkintoasters.backend.lobby.Seats.VoteKind;
import xyz.zlatanov.frakkintoasters.projection.Viewer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SeatsTest extends BackendTest {

    @Autowired
    Lobby        lobby;
    @Autowired
    GameSessions sessions;

    @Test
    void theHostRemovesAUserBeforeTheStartAndABanKeepsHimOut() {
        val host = createUser("Adama");
        val room = lobby.host(host, "Galactica");
        val tigh = createUser("Tigh");
        val baltar = createUser("Baltar");
        lobby.join(room.id(), tigh, Role.PLAYER);
        lobby.join(room.id(), baltar, Role.OBSERVER);

        assertRefused(() -> lobby.kick(room.id(), tigh, baltar, false)); // not the host
        assertRefused(() -> lobby.kick(room.id(), host, host, false));
        lobby.kick(room.id(), host, tigh, false);
        val banned = lobby.kick(room.id(), host, baltar, true);

        assertEquals(List.of(host), users(banned.players()));
        assertEquals(List.of(), banned.observers());
        lobby.join(room.id(), tigh, Role.PLAYER); // a kick alone lets him back
        assertRefused(() -> lobby.join(room.id(), baltar, Role.OBSERVER));
    }

    @Test
    void theHostCancelsHisLobby() {
        val host = createUser("Cain");
        val room = lobby.host(host, "Pegasus");
        val fisk = createUser("Fisk");
        lobby.join(room.id(), fisk, Role.PLAYER);
        assertRefused(() -> lobby.cancel(room.id(), fisk));

        val cancelled = lobby.cancel(room.id(), host);

        assertEquals(GameStatus.FINISHED, cancelled.status());
        assertEquals(EndReason.CANCELLED, cancelled.endReason());
        assertFalse(ids(lobby.games(fisk).mine()).contains(room.id()));
        assertFalse(ids(lobby.games(createUser("Shaw")).open()).contains(room.id()));
        assertRefused(() -> lobby.join(room.id(), createUser("Garner"), Role.PLAYER));
        assertRefused(() -> lobby.start(room.id(), host));
    }

    @Test
    void aPlayerLeavesHisSeatAndAnObserverTakesIt() {
        val game = runningGame(4);
        val roslin = game.users().get(1);
        val dualla = createUser("Dualla");
        lobby.join(game.id(), dualla, Role.OBSERVER);

        val left = lobby.leave(game.id(), roslin);

        assertEquals(List.of(2), left.vacantSeats());
        assertTrue(users(left.observers()).contains(roslin));
        assertEquals(1, summary(dualla, game.id()).vacantSeats());
        assertRefused(() -> lobby.takeSeat(game.id(), dualla, 5)); // no such seat
        assertRefused(() -> lobby.takeSeat(game.id(), game.users().get(2), 2)); // holds one already

        val taken = lobby.takeSeat(game.id(), dualla, 2);

        assertEquals(List.of(), taken.vacantSeats());
        assertEquals(dualla, holder(taken, 2));
        assertEquals(new Viewer.PlayerViewer(2), viewerOf(game.id(), dualla));
        val history = taken.seatHistory().stream().filter(holder -> holder.playerNumber() == 2).toList();
        assertEquals(List.of(roslin, dualla), history.stream().map(SeatHolder::userId).toList());
        assertEquals(LeaveReason.LEFT, history.getFirst().reason());
        assertNull(history.getLast().until());
    }

    @Test
    void aUserWhoHeldASeatMayOnlyTakeThatOneBack() {
        val game = runningGame(3);
        val roslin = game.users().get(1);
        val starbuck = game.users().get(2);
        lobby.leave(game.id(), roslin);
        lobby.leave(game.id(), starbuck);

        assertRefused(() -> lobby.takeSeat(game.id(), roslin, 3));
        lobby.takeSeat(game.id(), roslin, 2);
        val back = lobby.takeSeat(game.id(), starbuck, 3);

        assertEquals(roslin, holder(back, 2));
        assertEquals(starbuck, holder(back, 3));
    }

    @Test
    void aVoteVacatesASeatWithMoreThanHalfOfTheOtherPlayers() {
        val game = runningGame(4);
        val users = game.users();
        val apollo = users.get(3);

        var room = lobby.vote(game.id(), users.get(1), VoteKind.VACATE, 4);
        assertEquals(List.of(new Lobby.Vote(VoteKind.VACATE, 4, List.of(users.get(1)), 2)), room.votes());
        assertRefused(() -> lobby.vote(game.id(), apollo, VoteKind.VACATE, 4)); // his own seat: he leaves instead
        assertRefused(() -> lobby.vote(game.id(), createUser("Gaeta"), VoteKind.VACATE, 4)); // no player
        room = lobby.withdraw(game.id(), users.get(1), VoteKind.VACATE, 4);
        assertEquals(List.of(), room.votes());
        lobby.vote(game.id(), users.get(1), VoteKind.VACATE, 4);

        room = lobby.vote(game.id(), users.get(2), VoteKind.VACATE, 4);

        assertEquals(List.of(4), room.vacantSeats());
        assertEquals(List.of(), room.votes());
        assertTrue(users(room.observers()).contains(apollo));
        assertEquals(new Viewer.Observer(), viewerOf(game.id(), apollo));
    }

    @Test
    void theHostsVotePassesAtOnce() {
        val game = runningGame(3);

        val room = lobby.vote(game.id(), game.users().getFirst(), VoteKind.VACATE, 3);

        assertEquals(List.of(3), room.vacantSeats());
        assertEquals(LeaveReason.VOTED_OUT, room.seatHistory().stream()
                .filter(holder -> holder.playerNumber() == 3).findFirst().orElseThrow().reason());
    }

    @Test
    void theHostBansAPlayerFromARunningGame() {
        val game = runningGame(3);
        val starbuck = game.users().get(2);

        val room = lobby.kick(game.id(), game.users().getFirst(), starbuck, true);

        assertEquals(List.of(3), room.vacantSeats());
        assertFalse(users(room.observers()).contains(starbuck));
        assertRefused(() -> lobby.takeSeat(game.id(), starbuck, 3));
        assertRefused(() -> lobby.join(game.id(), starbuck, Role.OBSERVER));
    }

    @Test
    void aVoteOfMoreThanHalfOfThePlayersAbandonsTheGame() {
        val game = runningGame(4);
        val users = game.users();
        lobby.vote(game.id(), users.get(1), VoteKind.ABANDON, 0);
        var room = lobby.vote(game.id(), users.get(2), VoteKind.ABANDON, 0);
        assertEquals(GameStatus.RUNNING, room.status());
        assertEquals(3, room.votes().getFirst().needed());

        room = lobby.vote(game.id(), users.get(3), VoteKind.ABANDON, 0);

        assertEquals(GameStatus.FINISHED, room.status());
        assertEquals(EndReason.ABANDONED, room.endReason());
        assertNull(room.winnerTeam());
        for (int seat = 1; seat <= 4; seat++) {
            val game1 = sessions.status(game.id(), Viewer.player(seat)).game();
            assertNull(game1.input());
            assertEquals(List.of(), game1.anytime());
            assertNull(game1.outcome());
        }
        // it takes no command, also once it is restored from the rows
        sessions.evict(game.id());
        val status = sessions.status(game.id(), Viewer.player(1));
        assertNull(status.game().input());
        val rejected = (Rejected) sessions.choose(game.id(), Viewer.player(1), status.token(), 0);
        assertEquals(Reason.INVALID, rejected.reason());
        assertRefused(() -> lobby.vote(game.id(), users.get(1), VoteKind.ABANDON, 0));
    }

    @Test
    void aFinishedGameHasNoSeatToTake() {
        val game = runningGame(3);
        lobby.leave(game.id(), game.users().get(2));
        lobby.vote(game.id(), game.users().getFirst(), VoteKind.ABANDON, 0);

        assertEquals(List.of(), lobby.room(game.id()).vacantSeats());
        assertRefused(() -> lobby.takeSeat(game.id(), createUser("Ellen"), 3));
    }

    @Test
    void aRematchReopensTheRoomWithTheSameSeats() {
        val game = runningGame(3);
        val host = game.users().getFirst();
        val dualla = createUser("Dualla");
        lobby.leave(game.id(), game.users().get(1));
        lobby.takeSeat(game.id(), dualla, 2);
        lobby.vote(game.id(), host, VoteKind.ABANDON, 0);
        assertRefused(() -> lobby.rematch(game.id(), game.users().get(2)));

        val rematch = lobby.rematch(game.id(), host);

        assertEquals(GameStatus.LOBBY, rematch.status());
        assertEquals("Rematch: Galactica", rematch.name());
        assertEquals(List.of(host, dualla, game.users().get(2)), users(rematch.players()));
        assertEquals(List.of(1, 2, 3), rematch.players().stream().map(Participant::playerNumber).toList());
        assertEquals(rematch.id(), lobby.room(game.id()).rematchId());
        assertRefused(() -> lobby.rematch(game.id(), host));
    }

    private record Running(UUID id, List<UUID> users) {
    }

    private Running runningGame(int players) {
        val names = List.of("Adama", "Roslin", "Starbuck", "Apollo", "Tigh", "Helo", "Athena");
        val users = new ArrayList<UUID>();
        users.add(createUser(names.getFirst()));
        val room = lobby.host(users.getFirst(), "Galactica");
        for (int i = 1; i < players; i++) {
            users.add(createUser(names.get(i)));
            lobby.join(room.id(), users.getLast(), Role.PLAYER);
        }
        lobby.start(room.id(), users.getFirst());
        return new Running(room.id(), users);
    }

    private Viewer viewerOf(UUID gameId, UUID userId) {
        val participant = lobby.room(gameId).players().stream().filter(player -> player.userId().equals(userId)).findFirst();
        return participant.<Viewer>map(player -> Viewer.player(player.playerNumber())).orElse(Viewer.observer());
    }

    private Lobby.GameSummary summary(UUID userId, UUID gameId) {
        val games = lobby.games(userId);
        return java.util.stream.Stream.of(games.open(), games.mine(), games.running())
                .flatMap(List::stream)
                .filter(game -> game.id().equals(gameId))
                .findFirst()
                .orElseThrow();
    }

    private static UUID holder(Room room, int seat) {
        return room.players().stream().filter(player -> player.playerNumber() == seat).findFirst().orElseThrow().userId();
    }

    private static List<UUID> users(List<Participant> participants) {
        return participants.stream().map(Participant::userId).toList();
    }

    private static List<UUID> ids(List<Lobby.GameSummary> games) {
        return games.stream().map(Lobby.GameSummary::id).toList();
    }

    private static void assertRefused(Runnable call) {
        assertThrows(ResponseStatusException.class, call::run);
    }
}
