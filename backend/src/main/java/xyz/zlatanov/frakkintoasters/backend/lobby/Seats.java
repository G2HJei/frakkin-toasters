package xyz.zlatanov.frakkintoasters.backend.lobby;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class Seats {

    private final JdbcClient jdbc;

    public enum LeaveReason {
        LEFT,
        VOTED_OUT,
        KICKED,
        BANNED
    }

    public enum VoteKind {
        VACATE,
        ABANDON
    }

    public record SeatHolder(int playerNumber, UUID userId, String name, Instant since, Instant until, LeaveReason reason) {
    }

    public record Ballot(VoteKind kind, int playerNumber, UUID userId) {
    }

    public void take(UUID gameBundleId, int playerNumber, UUID userId) {
        jdbc.sql("""
                        INSERT INTO seat_holder (game_bundle_id, player_number, user_id, since)
                        VALUES (:game, :seat, :user, :now)""")
                .param("game", gameBundleId)
                .param("seat", playerNumber)
                .param("user", userId)
                .param("now", Timestamp.from(Instant.now()))
                .update();
    }

    public void leave(UUID gameBundleId, int playerNumber, LeaveReason reason) {
        jdbc.sql("""
                        UPDATE seat_holder SET until = :now, reason = :reason
                        WHERE game_bundle_id = :game AND player_number = :seat AND until IS NULL""")
                .param("game", gameBundleId)
                .param("seat", playerNumber)
                .param("reason", reason.name())
                .param("now", Timestamp.from(Instant.now()))
                .update();
        jdbc.sql("DELETE FROM vote WHERE game_bundle_id = :game AND kind = 'VACATE' AND player_number = :seat")
                .param("game", gameBundleId)
                .param("seat", playerNumber)
                .update();
    }

    public List<SeatHolder> history(UUID gameBundleId) {
        return jdbc.sql("""
                        SELECT s.*, u.name FROM seat_holder s JOIN app_user u ON u.id = s.user_id
                        WHERE s.game_bundle_id = :game
                        ORDER BY s.player_number, s.since""")
                .param("game", gameBundleId)
                .query((row, ignored) -> {
                    var until = row.getTimestamp("until");
                    var reason = row.getString("reason");
                    return new SeatHolder(row.getInt("player_number"), row.getObject("user_id", UUID.class),
                            row.getString("name"), row.getTimestamp("since").toInstant(),
                            until == null ? null : until.toInstant(), reason == null ? null : LeaveReason.valueOf(reason));
                })
                .list();
    }

    public void ban(UUID gameBundleId, UUID userId) {
        jdbc.sql("INSERT INTO game_ban (game_bundle_id, user_id) VALUES (:game, :user) ON CONFLICT DO NOTHING")
                .param("game", gameBundleId)
                .param("user", userId)
                .update();
    }

    public boolean banned(UUID gameBundleId, UUID userId) {
        return jdbc.sql("SELECT count(*) FROM game_ban WHERE game_bundle_id = :game AND user_id = :user")
                .param("game", gameBundleId)
                .param("user", userId)
                .query(Long.class)
                .single() > 0;
    }

    public void vote(UUID gameBundleId, VoteKind kind, int playerNumber, UUID userId) {
        jdbc.sql("""
                        INSERT INTO vote (game_bundle_id, kind, player_number, user_id, created_at)
                        VALUES (:game, :kind, :seat, :user, :now) ON CONFLICT DO NOTHING""")
                .param("game", gameBundleId)
                .param("kind", kind.name())
                .param("seat", playerNumber)
                .param("user", userId)
                .param("now", Timestamp.from(Instant.now()))
                .update();
    }

    public void withdraw(UUID gameBundleId, VoteKind kind, int playerNumber, UUID userId) {
        jdbc.sql("""
                        DELETE FROM vote
                        WHERE game_bundle_id = :game AND kind = :kind AND player_number = :seat AND user_id = :user""")
                .param("game", gameBundleId)
                .param("kind", kind.name())
                .param("seat", playerNumber)
                .param("user", userId)
                .update();
    }

    /**
     * Drops a user's votes, once he holds no seat: only the seated players vote.
     */
    public void withdrawAll(UUID gameBundleId, UUID userId) {
        jdbc.sql("DELETE FROM vote WHERE game_bundle_id = :game AND user_id = :user")
                .param("game", gameBundleId)
                .param("user", userId)
                .update();
    }

    public void clearVotes(UUID gameBundleId, VoteKind kind, int playerNumber) {
        jdbc.sql("DELETE FROM vote WHERE game_bundle_id = :game AND kind = :kind AND player_number = :seat")
                .param("game", gameBundleId)
                .param("kind", kind.name())
                .param("seat", playerNumber)
                .update();
    }

    public List<Ballot> ballots(UUID gameBundleId) {
        return jdbc.sql("SELECT * FROM vote WHERE game_bundle_id = :game ORDER BY kind, player_number, created_at")
                .param("game", gameBundleId)
                .query((row, ignored) -> new Ballot(VoteKind.valueOf(row.getString("kind")), row.getInt("player_number"),
                        row.getObject("user_id", UUID.class)))
                .list();
    }
}
