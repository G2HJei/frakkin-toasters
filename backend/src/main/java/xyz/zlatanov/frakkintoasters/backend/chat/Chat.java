package xyz.zlatanov.frakkintoasters.backend.chat;

import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import xyz.zlatanov.frakkintoasters.backend.game.GameBundle.GameMode;
import xyz.zlatanov.frakkintoasters.backend.game.GameBundles;
import xyz.zlatanov.frakkintoasters.backend.lobby.Participants;
import xyz.zlatanov.frakkintoasters.projection.Viewer;

import java.sql.Timestamp;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * One chat for everyone at the table, players and observers: no private messages, the rules forbid them.
 */
@Service
public class Chat {

    public static final int MAX_LENGTH = 500;
    public static final int HISTORY    = 300;
    static final int        BURST      = 5;
    static final Duration   WINDOW     = Duration.ofSeconds(10);

    private final JdbcClient                  jdbc;
    private final GameBundles                 bundles;
    private final Participants                participants;
    private final Clock                       clock;
    private final Map<String, Deque<Instant>> recent = new ConcurrentHashMap<>();

    public record ChatLine(long id, String userId, String name, Integer playerNumber, String text, String at) {
    }

    public static final class Refused extends RuntimeException {

        public Refused(String message) {
            super(message);
        }
    }

    @Autowired
    public Chat(JdbcClient jdbc, GameBundles bundles, Participants participants) {
        this(jdbc, bundles, participants, Clock.systemUTC());
    }

    Chat(JdbcClient jdbc, GameBundles bundles, Participants participants, Clock clock) {
        this.jdbc = jdbc;
        this.bundles = bundles;
        this.participants = participants;
        this.clock = clock;
    }

    public static boolean reads(Viewer viewer) {
        return !(viewer instanceof Viewer.Sandbox);
    }

    public ChatLine post(UUID gameBundleId, UUID userId, String text) {
        val bundle = bundles.find(gameBundleId).orElseThrow(() -> new Refused("No such game"));
        if (bundle.mode() == GameMode.SANDBOX) {
            throw new Refused("A sandbox has no chat");
        }
        val participant = participants.find(gameBundleId, userId)
                .orElseThrow(() -> new Refused("Only the players and observers of the game write in its chat"));
        val message = text == null ? "" : text.strip();
        if (message.isEmpty()) {
            throw new Refused("The message is empty");
        }
        if (message.length() > MAX_LENGTH) {
            throw new Refused("A message has at most " + MAX_LENGTH + " characters");
        }
        throttle(gameBundleId, userId);
        val at = clock.instant();
        val id = jdbc.sql("""
                        INSERT INTO chat_message (game_bundle_id, user_id, player_number, text, created_at)
                        VALUES (:game, :user, :seat, :text, :at)
                        RETURNING id""")
                .param("game", gameBundleId)
                .param("user", userId)
                .param("seat", participant.playerNumber())
                .param("text", message)
                .param("at", Timestamp.from(at))
                .query(Long.class)
                .single();
        return new ChatLine(id, userId.toString(), participant.name(), participant.playerNumber(), message, at.toString());
    }

    public List<ChatLine> history(UUID gameBundleId, Viewer viewer) {
        if (!reads(viewer)) {
            return List.of();
        }
        return jdbc.sql("""
                        SELECT * FROM (
                            SELECT c.*, u.name FROM chat_message c JOIN app_user u ON u.id = c.user_id
                            WHERE c.game_bundle_id = :game
                            ORDER BY c.id DESC LIMIT :limit) last
                        ORDER BY id""")
                .param("game", gameBundleId)
                .param("limit", HISTORY)
                .query((row, ignored) -> new ChatLine(row.getLong("id"), row.getObject("user_id", UUID.class).toString(),
                        row.getString("name"), (Integer) row.getObject("player_number"), row.getString("text"),
                        row.getTimestamp("created_at").toInstant().toString()))
                .list();
    }

    private void throttle(UUID gameBundleId, UUID userId) {
        val now = clock.instant();
        val times = recent.computeIfAbsent(gameBundleId + ":" + userId, ignored -> new ArrayDeque<>());
        synchronized (times) {
            while (!times.isEmpty() && !times.peekFirst().isAfter(now.minus(WINDOW))) {
                times.pollFirst();
            }
            if (times.size() >= BURST) {
                throw new Refused("Slow down: at most " + BURST + " messages in " + WINDOW.toSeconds() + " seconds");
            }
            times.addLast(now);
        }
    }
}
