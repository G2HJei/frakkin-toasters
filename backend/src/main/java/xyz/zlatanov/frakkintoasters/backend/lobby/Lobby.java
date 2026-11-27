package xyz.zlatanov.frakkintoasters.backend.lobby;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;
import xyz.zlatanov.frakkintoasters.backend.game.GameBundle;
import xyz.zlatanov.frakkintoasters.backend.game.GameBundle.EndReason;
import xyz.zlatanov.frakkintoasters.backend.game.GameBundle.GameMode;
import xyz.zlatanov.frakkintoasters.backend.game.GameBundle.GameStatus;
import xyz.zlatanov.frakkintoasters.backend.game.GameBundles;
import xyz.zlatanov.frakkintoasters.backend.game.GameSessions;
import xyz.zlatanov.frakkintoasters.backend.journal.JournalCodec;
import xyz.zlatanov.frakkintoasters.backend.lobby.Participants.Participant;
import xyz.zlatanov.frakkintoasters.backend.lobby.Seats.Ballot;
import xyz.zlatanov.frakkintoasters.backend.lobby.Seats.LeaveReason;
import xyz.zlatanov.frakkintoasters.backend.lobby.Seats.SeatHolder;
import xyz.zlatanov.frakkintoasters.backend.lobby.Seats.VoteKind;
import xyz.zlatanov.frakkintoasters.state.Team;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;

/**
 * The start closes the seats up to 1..n, because the engine numbers the players that way and the numbers are the
 * clockwise order.
 */
@Service
@RequiredArgsConstructor
public class Lobby {

    public static final int MIN_PLAYERS = 3;
    public static final int MAX_PLAYERS = 7;
    static final int        MAX_NAME    = 60;

    private final GameBundles         bundles;
    private final Participants        participants;
    private final Seats               seats;
    private final Presence            presence;
    private final GameSessions        sessions;
    private final TransactionTemplate transaction;
    private final JdbcClient          jdbc;

    public enum Role {
        PLAYER,
        OBSERVER
    }

    public record Room(UUID id, String name, GameStatus status, GameMode mode, UUID hostUserId,
                       List<Participant> players, List<Participant> observers, Team winnerTeam,
                       boolean openable, String unavailableReason, Long seed, EndReason endReason,
                       Integer playerCount, List<Integer> vacantSeats, List<Vote> votes, List<SeatHolder> seatHistory,
                       UUID rematchId, List<UUID> online) {
    }

    public record Vote(VoteKind kind, int playerNumber, List<UUID> voters, int needed) {
    }

    public record GameSummary(UUID id, String name, GameStatus status, GameMode mode, int turn, String hostName,
                              int players, Team winnerTeam, boolean participant, Integer myPlayerNumber,
                              boolean waitingForMe, Instant updatedAt, boolean openable, EndReason endReason,
                              int vacantSeats) {
    }

    public record Games(List<GameSummary> open, List<GameSummary> mine, List<GameSummary> running) {
    }

    // What a change of the seats leaves to do once its transaction is committed: the connections whose viewer changed
    // reconnect, and an abandon ends the game under its lock, which mustn't wait for the row this transaction locks.
    private record Change(Set<UUID> reconnect, boolean abandon) {

        static Change none() {
            return new Change(Set.of(), false);
        }

        static Change reconnect(UUID userId) {
            return new Change(Set.of(userId), false);
        }
    }

    public Room host(UUID userId, String name) {
        val gameName = name == null || name.isBlank() ? "A new game" : name.strip();
        if (gameName.length() > MAX_NAME) {
            throw refused(HttpStatus.BAD_REQUEST, "A name has at most " + MAX_NAME + " characters");
        }
        val id = transaction.execute(ignored -> {
            val created = bundles.create(gameName, userId, JournalCodec.FORMAT_VERSION);
            participants.add(created, userId, 1);
            return created;
        });
        return room(id);
    }

    public Room room(UUID gameBundleId) {
        val bundle = bundles.find(gameBundleId).orElseThrow(() -> refused(HttpStatus.NOT_FOUND, "No such game"));
        val everyone = participants.of(gameBundleId);
        val players = everyone.stream().filter(Participant::isPlayer).toList();
        val unavailable = bundle.formatVersion() == JournalCodec.FORMAT_VERSION
                ? null
                : "Saved by another version of the game (format " + bundle.formatVersion() + ")";
        val here = presence.online(gameBundleId, bundle.status());
        return new Room(bundle.id(), bundle.name(), bundle.status(), bundle.mode(), bundle.hostUserId(),
                players, everyone.stream().filter(participant -> !participant.isPlayer()).toList(),
                bundle.winnerTeam(), unavailable == null, unavailable, bundle.seed(), bundle.endReason(),
                bundle.playerCount(), vacantSeats(bundle, players),
                bundle.status() == GameStatus.RUNNING ? votes(gameBundleId, players) : List.of(),
                seats.history(gameBundleId), bundle.rematchId(),
                everyone.stream().map(Participant::userId).filter(here::contains).toList());
    }

    /**
     * The user's room page polls: in a lobby, that is what tells he is there.
     */
    public Room roomFor(UUID gameBundleId, UUID userId) {
        presence.polled(gameBundleId, userId);
        return room(gameBundleId);
    }

    public Room join(UUID gameBundleId, UUID userId, Role role) {
        transaction.executeWithoutResult(ignored -> {
            val bundle = lock(gameBundleId);
            if (bundle.mode() == GameMode.SANDBOX) {
                throw refused(HttpStatus.FORBIDDEN, "Nobody else can join a sandbox");
            }
            if (bundle.endReason() == EndReason.CANCELLED) {
                throw refused(HttpStatus.CONFLICT, "The host cancelled this game");
            }
            if (seats.banned(gameBundleId, userId)) {
                throw refused(HttpStatus.FORBIDDEN, "The host banned you from this game");
            }
            val everyone = participants.of(gameBundleId);
            val me = find(everyone, userId);
            if (me.isPresent() && me.get().isPlayer() == (role == Role.PLAYER)) {
                throw refused(HttpStatus.CONFLICT, "You are in this game already");
            }
            if (me.isPresent() && bundle.status() != GameStatus.LOBBY) {
                throw refused(HttpStatus.CONFLICT, "You can't switch once the game has started");
            }
            if (role == Role.OBSERVER) {
                if (me.isPresent()) {
                    participants.seat(gameBundleId, userId, null);
                } else {
                    participants.add(gameBundleId, userId, null);
                }
                return;
            }
            if (bundle.status() != GameStatus.LOBBY) {
                throw refused(HttpStatus.CONFLICT, "Only observers can join once the game has started");
            }
            val taken = everyone.stream().filter(Participant::isPlayer).map(Participant::playerNumber).toList();
            val seat = IntStream.rangeClosed(1, MAX_PLAYERS).filter(n -> !taken.contains(n)).findFirst()
                    .orElseThrow(() -> refused(HttpStatus.CONFLICT, "The game is full"));
            if (me.isPresent()) {
                participants.seat(gameBundleId, userId, seat);
            } else {
                participants.add(gameBundleId, userId, seat);
            }
        });
        return room(gameBundleId);
    }

    public Room leave(UUID gameBundleId, UUID userId) {
        return change(gameBundleId, () -> {
            val bundle = lock(gameBundleId);
            val me = find(participants.of(gameBundleId), userId)
                    .orElseThrow(() -> refused(HttpStatus.CONFLICT, "You aren't in this game"));
            val host = bundle.hostUserId().equals(userId);
            if (bundle.status() == GameStatus.LOBBY) {
                if (host) {
                    throw refused(HttpStatus.CONFLICT, "The host can't leave his game");
                }
                participants.remove(gameBundleId, userId);
                return Change.none();
            }
            if (me.isPlayer()) {
                if (bundle.status() != GameStatus.RUNNING) {
                    throw refused(HttpStatus.CONFLICT, "The game is over");
                }
                vacate(gameBundleId, me, LeaveReason.LEFT);
                return Change.reconnect(userId);
            }
            if (host) {
                throw refused(HttpStatus.CONFLICT, "The host can't leave his game");
            }
            participants.remove(gameBundleId, userId);
            return Change.reconnect(userId);
        });
    }

    public Room kick(UUID gameBundleId, UUID hostId, UUID userId, boolean ban) {
        return change(gameBundleId, () -> {
            val bundle = lock(gameBundleId);
            requireHost(bundle, hostId, "Only the host removes a user");
            if (userId.equals(hostId)) {
                throw refused(HttpStatus.CONFLICT, "The host can't be removed");
            }
            if (bundle.status() == GameStatus.FINISHED) {
                throw refused(HttpStatus.CONFLICT, "The game is over");
            }
            val target = find(participants.of(gameBundleId), userId)
                    .orElseThrow(() -> refused(HttpStatus.NOT_FOUND, "The user isn't in this game"));
            if (bundle.status() == GameStatus.RUNNING && target.isPlayer()) {
                seats.leave(gameBundleId, target.playerNumber(), ban ? LeaveReason.BANNED : LeaveReason.KICKED);
            }
            seats.withdrawAll(gameBundleId, userId);
            participants.remove(gameBundleId, userId);
            if (ban) {
                seats.ban(gameBundleId, userId);
            }
            return Change.reconnect(userId);
        });
    }

    public Room cancel(UUID gameBundleId, UUID hostId) {
        transaction.executeWithoutResult(ignored -> {
            val bundle = lock(gameBundleId);
            requireHost(bundle, hostId, "Only the host cancels the game");
            if (bundle.status() != GameStatus.LOBBY) {
                throw refused(HttpStatus.CONFLICT, "Only a game that hasn't started can be cancelled");
            }
            bundles.end(gameBundleId, EndReason.CANCELLED);
        });
        return room(gameBundleId);
    }

    public Room takeSeat(UUID gameBundleId, UUID userId, int playerNumber) {
        return change(gameBundleId, () -> {
            val bundle = lock(gameBundleId);
            requireRunningGame(bundle);
            if (seats.banned(gameBundleId, userId)) {
                throw refused(HttpStatus.FORBIDDEN, "The host banned you from this game");
            }
            if (playerNumber < 1 || playerNumber > bundle.playerCount()) {
                throw refused(HttpStatus.BAD_REQUEST, "No seat " + playerNumber);
            }
            val everyone = participants.of(gameBundleId);
            if (everyone.stream().anyMatch(participant -> Integer.valueOf(playerNumber).equals(participant.playerNumber()))) {
                throw refused(HttpStatus.CONFLICT, "The seat is taken");
            }
            val me = find(everyone, userId);
            if (me.isPresent() && me.get().isPlayer()) {
                throw refused(HttpStatus.CONFLICT, "You hold a seat already");
            }
            val held = seats.history(gameBundleId).stream()
                    .filter(holder -> holder.userId().equals(userId))
                    .map(SeatHolder::playerNumber)
                    .filter(seat -> seat != playerNumber)
                    .findFirst();
            if (held.isPresent()) {
                throw refused(HttpStatus.CONFLICT, "You held seat " + held.get() + ": only that one can be yours");
            }
            if (me.isPresent()) {
                participants.seat(gameBundleId, userId, playerNumber);
            } else {
                participants.add(gameBundleId, userId, playerNumber);
            }
            seats.take(gameBundleId, playerNumber, userId);
            return Change.reconnect(userId);
        });
    }

    public Room vote(UUID gameBundleId, UUID userId, VoteKind kind, int playerNumber) {
        return change(gameBundleId, () -> {
            val bundle = lock(gameBundleId);
            requireRunningGame(bundle);
            val players = participants.of(gameBundleId).stream().filter(Participant::isPlayer).toList();
            val host = bundle.hostUserId().equals(userId);
            if (!host && find(players, userId).isEmpty()) {
                throw refused(HttpStatus.FORBIDDEN, "Only the players vote");
            }
            if (kind == VoteKind.ABANDON) {
                if (!host) {
                    seats.vote(gameBundleId, kind, 0, userId);
                }
                return new Change(Set.of(), host || passes(gameBundleId, kind, 0, players));
            }
            val holder = players.stream()
                    .filter(player -> player.playerNumber() == playerNumber)
                    .findFirst()
                    .orElseThrow(() -> refused(HttpStatus.CONFLICT, "Nobody holds seat " + playerNumber));
            if (holder.userId().equals(userId)) {
                throw refused(HttpStatus.CONFLICT, "Leave your seat instead");
            }
            if (!host) {
                seats.vote(gameBundleId, kind, playerNumber, userId);
            }
            if (host || passes(gameBundleId, kind, playerNumber, players)) {
                vacate(gameBundleId, holder, LeaveReason.VOTED_OUT);
                return Change.reconnect(holder.userId());
            }
            return Change.none();
        });
    }

    public Room withdraw(UUID gameBundleId, UUID userId, VoteKind kind, int playerNumber) {
        transaction.executeWithoutResult(ignored -> {
            lock(gameBundleId);
            seats.withdraw(gameBundleId, kind, kind == VoteKind.ABANDON ? 0 : playerNumber, userId);
        });
        return room(gameBundleId);
    }

    public Room rematch(UUID gameBundleId, UUID hostId) {
        val id = transaction.execute(ignored -> {
            val bundle = lock(gameBundleId);
            requireHost(bundle, hostId, "Only the host opens a rematch");
            if (bundle.mode() != GameMode.STANDARD || bundle.status() != GameStatus.FINISHED
                    || bundle.endReason() == EndReason.CANCELLED) {
                throw refused(HttpStatus.CONFLICT, "Only a game that was played has a rematch");
            }
            if (bundle.rematchId() != null) {
                throw refused(HttpStatus.CONFLICT, "The rematch is open already");
            }
            val name = bundle.name().startsWith("Rematch: ") ? bundle.name() : "Rematch: " + bundle.name();
            val created = bundles.create(name.length() > MAX_NAME ? name.substring(0, MAX_NAME) : name, hostId,
                    JournalCodec.FORMAT_VERSION);
            val players = participants.of(gameBundleId).stream().filter(Participant::isPlayer).toList();
            players.forEach(player -> participants.add(created, player.userId(), player.playerNumber()));
            if (find(players, hostId).isEmpty()) {
                participants.add(created, hostId, null);
            }
            bundles.rematch(gameBundleId, created);
            return created;
        });
        return room(id);
    }

    public Room start(UUID gameBundleId, UUID userId) {
        val playerCount = transaction.execute(ignored -> {
            val bundle = lock(gameBundleId);
            requireHost(bundle, userId, "Only the host starts the game");
            if (bundle.status() != GameStatus.LOBBY) {
                throw refused(HttpStatus.CONFLICT, "The game has started already");
            }
            val players = participants.of(gameBundleId).stream().filter(Participant::isPlayer).toList();
            if (players.size() < MIN_PLAYERS || players.size() > MAX_PLAYERS) {
                throw refused(HttpStatus.CONFLICT, "A game needs " + MIN_PLAYERS + " to " + MAX_PLAYERS + " players");
            }
            // ascending, so a seat moves down to one that is free already
            for (int i = 0; i < players.size(); i++) {
                participants.seat(gameBundleId, players.get(i).userId(), i + 1);
                seats.take(gameBundleId, i + 1, players.get(i).userId());
            }
            bundles.playerCount(gameBundleId, players.size());
            return players.size();
        });
        sessions.start(gameBundleId, playerCount);
        return room(gameBundleId);
    }

    public Games games(UUID userId) {
        val summaries = jdbc.sql("""
                        SELECT g.*, h.name AS host_name,
                               (SELECT count(*) FROM participant p WHERE p.game_bundle_id = g.id
                                AND p.player_number IS NOT NULL) AS players,
                               me.user_id AS my_user_id, me.player_number AS my_player_number
                        FROM game_bundle g
                        JOIN app_user h ON h.id = g.host_user_id
                        LEFT JOIN participant me ON me.game_bundle_id = g.id AND me.user_id = :user
                        WHERE (me.user_id IS NOT NULL
                           OR (g.mode = 'STANDARD' AND g.status IN ('LOBBY', 'RUNNING')))
                          AND g.end_reason IS DISTINCT FROM 'CANCELLED'
                        ORDER BY g.updated_at DESC
                        LIMIT 200""")
                .param("user", userId)
                .query((row, number) -> {
                    val bundle = GameBundles.map(row, number);
                    val participant = row.getObject("my_user_id") != null;
                    val seat = (Integer) row.getObject("my_player_number");
                    val players = row.getInt("players");
                    val waitingForMe = bundle.status() == GameStatus.RUNNING && participant
                            && (bundle.mode() == GameMode.SANDBOX
                            || seat != null && seat.equals(bundle.waitingForPlayerNumber()));
                    val vacant = bundle.status() == GameStatus.RUNNING && bundle.mode() == GameMode.STANDARD
                            && bundle.playerCount() != null ? bundle.playerCount() - players : 0;
                    return new GameSummary(bundle.id(), bundle.name(), bundle.status(), bundle.mode(), bundle.turn(),
                            row.getString("host_name"), players, bundle.winnerTeam(), participant, seat,
                            waitingForMe, bundle.updatedAt(), bundle.formatVersion() == JournalCodec.FORMAT_VERSION,
                            bundle.endReason(), vacant);
                })
                .list();
        return new Games(
                summaries.stream().filter(game -> !game.participant() && game.status() == GameStatus.LOBBY).toList(),
                summaries.stream().filter(GameSummary::participant).toList(),
                summaries.stream().filter(game -> !game.participant() && game.status() == GameStatus.RUNNING).toList());
    }

    // The change runs in a transaction that locks the game's row; what it leaves to do runs once it is committed.
    private Room change(UUID gameBundleId, java.util.function.Supplier<Change> change) {
        val done = transaction.execute(ignored -> change.get());
        if (done.abandon()) {
            sessions.abandon(gameBundleId);
        }
        if (!done.reconnect().isEmpty()) {
            sessions.reconnect(gameBundleId, done.reconnect());
        }
        return room(gameBundleId);
    }

    private void vacate(UUID gameBundleId, Participant holder, LeaveReason reason) {
        participants.seat(gameBundleId, holder.userId(), null);
        seats.leave(gameBundleId, holder.playerNumber(), reason);
        seats.withdrawAll(gameBundleId, holder.userId());
    }

    private boolean passes(UUID gameBundleId, VoteKind kind, int playerNumber, List<Participant> players) {
        val vote = votes(gameBundleId, players).stream()
                .filter(open -> open.kind() == kind && open.playerNumber() == playerNumber)
                .findFirst();
        return vote.isPresent() && vote.get().voters().size() >= vote.get().needed();
    }

    private List<Vote> votes(UUID gameBundleId, List<Participant> players) {
        val seated = new HashSet<UUID>();
        players.forEach(player -> seated.add(player.userId()));
        val byVote = new LinkedHashMap<String, List<Ballot>>();
        for (val ballot : seats.ballots(gameBundleId)) {
            if (seated.contains(ballot.userId())) {
                byVote.computeIfAbsent(ballot.kind() + ":" + ballot.playerNumber(), ignored -> new ArrayList<>()).add(ballot);
            }
        }
        return byVote.values().stream().map(ballots -> {
            val first = ballots.getFirst();
            val voters = first.kind() == VoteKind.ABANDON ? players.size() : players.size() - 1;
            return new Vote(first.kind(), first.playerNumber(), ballots.stream().map(Ballot::userId).toList(), voters / 2 + 1);
        }).toList();
    }

    private static List<Integer> vacantSeats(GameBundle bundle, List<Participant> players) {
        if (bundle.status() != GameStatus.RUNNING || bundle.mode() != GameMode.STANDARD || bundle.playerCount() == null) {
            return List.of();
        }
        val taken = players.stream().map(Participant::playerNumber).toList();
        return IntStream.rangeClosed(1, bundle.playerCount()).filter(seat -> !taken.contains(seat)).boxed().toList();
    }

    private static Optional<Participant> find(List<Participant> participants, UUID userId) {
        return participants.stream().filter(participant -> participant.userId().equals(userId)).findFirst();
    }

    private static void requireHost(GameBundle bundle, UUID userId, String reason) {
        if (!bundle.hostUserId().equals(userId)) {
            throw refused(HttpStatus.FORBIDDEN, reason);
        }
    }

    private static void requireRunningGame(GameBundle bundle) {
        if (bundle.mode() != GameMode.STANDARD) {
            throw refused(HttpStatus.FORBIDDEN, "A sandbox has one user, who holds every seat");
        }
        if (bundle.status() != GameStatus.RUNNING) {
            throw refused(HttpStatus.CONFLICT, "The game isn't running");
        }
    }

    private GameBundle lock(UUID gameBundleId) {
        return bundles.findForUpdate(gameBundleId).orElseThrow(() -> refused(HttpStatus.NOT_FOUND, "No such game"));
    }

    private static ResponseStatusException refused(HttpStatus status, String reason) {
        return new ResponseStatusException(status, reason);
    }
}
