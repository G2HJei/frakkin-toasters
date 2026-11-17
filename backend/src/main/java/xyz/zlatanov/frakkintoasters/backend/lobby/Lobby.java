package xyz.zlatanov.frakkintoasters.backend.lobby;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;
import xyz.zlatanov.frakkintoasters.backend.game.GameBundle;
import xyz.zlatanov.frakkintoasters.backend.game.GameBundle.GameMode;
import xyz.zlatanov.frakkintoasters.backend.game.GameBundle.GameStatus;
import xyz.zlatanov.frakkintoasters.backend.game.GameBundles;
import xyz.zlatanov.frakkintoasters.backend.game.GameSessions;
import xyz.zlatanov.frakkintoasters.backend.journal.JournalCodec;
import xyz.zlatanov.frakkintoasters.backend.lobby.Participants.Participant;
import xyz.zlatanov.frakkintoasters.state.Team;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

/**
 * Hosting, joining and starting games. A user is in a game once, as a player or an observer. Players take the next
 * free seat, 7 at most, until the game starts; observers can join any time. The start closes the seats up to 1..n,
 * because the engine numbers the players that way and the numbers are the clockwise order.
 */
@Service
@RequiredArgsConstructor
public class Lobby {

    public static final int MIN_PLAYERS = 3;
    public static final int MAX_PLAYERS = 7;

    private final GameBundles         bundles;
    private final Participants        participants;
    private final GameSessions        sessions;
    private final TransactionTemplate transaction;
    private final JdbcClient          jdbc;

    public enum Role {
        PLAYER,
        OBSERVER
    }

    public record Room(UUID id, String name, GameStatus status, GameMode mode, UUID hostUserId,
                       List<Participant> players, List<Participant> observers, Team winnerTeam,
                       boolean openable, String unavailableReason, Long seed) {
    }

    /**
     * @param myPlayerNumber the user's seat, {@code null} when he observes or isn't in the game
     * @param waitingForMe   the game waits for the user
     */
    public record GameSummary(UUID id, String name, GameStatus status, GameMode mode, int turn, String hostName,
                              int players, Team winnerTeam, boolean participant, Integer myPlayerNumber,
                              boolean waitingForMe, Instant updatedAt, boolean openable) {
    }

    /**
     * @param open    the lobbies the user can join
     * @param mine    the games the user is in, his sandboxes included
     * @param running the games under way the user can watch
     */
    public record Games(List<GameSummary> open, List<GameSummary> mine, List<GameSummary> running) {
    }

    public Room host(UUID userId, String name) {
        val gameName = name == null || name.isBlank() ? "A new game" : name.strip();
        if (gameName.length() > 60) {
            throw conflict(HttpStatus.BAD_REQUEST, "A name has at most 60 characters");
        }
        val id = transaction.execute(ignored -> {
            val created = bundles.create(gameName, userId, JournalCodec.FORMAT_VERSION);
            participants.add(created, userId, 1);
            return created;
        });
        return room(id);
    }

    public Room room(UUID gameBundleId) {
        val bundle = bundles.find(gameBundleId).orElseThrow(() -> conflict(HttpStatus.NOT_FOUND, "No such game"));
        val everyone = participants.of(gameBundleId);
        val unavailable = bundle.formatVersion() == JournalCodec.FORMAT_VERSION
                ? null
                : "Saved by another version of the game (format " + bundle.formatVersion() + ")";
        return new Room(bundle.id(), bundle.name(), bundle.status(), bundle.mode(), bundle.hostUserId(),
                everyone.stream().filter(Participant::isPlayer).toList(),
                everyone.stream().filter(participant -> !participant.isPlayer()).toList(),
                bundle.winnerTeam(), unavailable == null, unavailable, bundle.seed());
    }

    public Room join(UUID gameBundleId, UUID userId, Role role) {
        transaction.executeWithoutResult(ignored -> {
            val bundle = lock(gameBundleId);
            if (bundle.mode() == GameMode.SANDBOX) {
                throw conflict(HttpStatus.FORBIDDEN, "Nobody else can join a sandbox");
            }
            val everyone = participants.of(gameBundleId);
            val me = everyone.stream().filter(participant -> participant.userId().equals(userId)).findFirst();
            if (me.isPresent() && me.get().isPlayer() == (role == Role.PLAYER)) {
                throw conflict(HttpStatus.CONFLICT, "You are in this game already");
            }
            if (me.isPresent() && bundle.status() != GameStatus.LOBBY) {
                throw conflict(HttpStatus.CONFLICT, "You can't switch once the game has started");
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
                throw conflict(HttpStatus.CONFLICT, "Only observers can join once the game has started");
            }
            val taken = everyone.stream().filter(Participant::isPlayer).map(Participant::playerNumber).toList();
            val seat = IntStream.rangeClosed(1, MAX_PLAYERS).filter(n -> !taken.contains(n)).findFirst()
                    .orElseThrow(() -> conflict(HttpStatus.CONFLICT, "The game is full"));
            if (me.isPresent()) {
                participants.seat(gameBundleId, userId, seat);
            } else {
                participants.add(gameBundleId, userId, seat);
            }
        });
        return room(gameBundleId);
    }

    public Room leave(UUID gameBundleId, UUID userId) {
        transaction.executeWithoutResult(ignored -> {
            val bundle = lock(gameBundleId);
            if (bundle.status() != GameStatus.LOBBY) {
                throw conflict(HttpStatus.CONFLICT, "A game can only be left before it starts");
            }
            if (bundle.hostUserId().equals(userId)) {
                throw conflict(HttpStatus.CONFLICT, "The host can't leave his game");
            }
            participants.remove(gameBundleId, userId);
        });
        return room(gameBundleId);
    }

    public Room start(UUID gameBundleId, UUID userId) {
        val playerCount = transaction.execute(ignored -> {
            val bundle = lock(gameBundleId);
            if (!bundle.hostUserId().equals(userId)) {
                throw conflict(HttpStatus.FORBIDDEN, "Only the host starts the game");
            }
            if (bundle.status() != GameStatus.LOBBY) {
                throw conflict(HttpStatus.CONFLICT, "The game has started already");
            }
            val players = participants.of(gameBundleId).stream().filter(Participant::isPlayer).toList();
            if (players.size() < MIN_PLAYERS || players.size() > MAX_PLAYERS) {
                throw conflict(HttpStatus.CONFLICT, "A game needs " + MIN_PLAYERS + " to " + MAX_PLAYERS + " players");
            }
            // ascending, so a seat moves down to one that is free already
            for (int i = 0; i < players.size(); i++) {
                participants.seat(gameBundleId, players.get(i).userId(), i + 1);
            }
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
                        WHERE me.user_id IS NOT NULL
                           OR (g.mode = 'STANDARD' AND g.status IN ('LOBBY', 'RUNNING'))
                        ORDER BY g.updated_at DESC
                        LIMIT 200""")
                .param("user", userId)
                .query((row, number) -> {
                    val bundle = GameBundles.map(row, number);
                    val participant = row.getObject("my_user_id") != null;
                    val seat = (Integer) row.getObject("my_player_number");
                    val waitingForMe = bundle.status() == GameStatus.RUNNING && participant
                            && (bundle.mode() == GameMode.SANDBOX
                            || seat != null && seat.equals(bundle.waitingForPlayerNumber()));
                    return new GameSummary(bundle.id(), bundle.name(), bundle.status(), bundle.mode(), bundle.turn(),
                            row.getString("host_name"), row.getInt("players"), bundle.winnerTeam(), participant, seat,
                            waitingForMe, bundle.updatedAt(), bundle.formatVersion() == JournalCodec.FORMAT_VERSION);
                })
                .list();
        return new Games(
                summaries.stream().filter(game -> !game.participant() && game.status() == GameStatus.LOBBY).toList(),
                summaries.stream().filter(GameSummary::participant).toList(),
                summaries.stream().filter(game -> !game.participant() && game.status() == GameStatus.RUNNING).toList());
    }

    private GameBundle lock(UUID gameBundleId) {
        return bundles.findForUpdate(gameBundleId).orElseThrow(() -> conflict(HttpStatus.NOT_FOUND, "No such game"));
    }

    private static ResponseStatusException conflict(HttpStatus status, String reason) {
        return new ResponseStatusException(status, reason);
    }
}
