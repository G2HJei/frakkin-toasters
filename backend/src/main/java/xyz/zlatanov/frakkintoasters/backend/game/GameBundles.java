package xyz.zlatanov.frakkintoasters.backend.game;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import xyz.zlatanov.frakkintoasters.backend.game.GameBundle.EndReason;
import xyz.zlatanov.frakkintoasters.backend.game.GameBundle.GameStatus;
import xyz.zlatanov.frakkintoasters.state.Team;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class GameBundles {

    private final JdbcClient jdbc;

    public UUID create(String name, UUID hostUserId, int formatVersion) {
        var id = UUID.randomUUID();
        var now = Timestamp.from(Instant.now());
        jdbc.sql("""
                        INSERT INTO game_bundle (id, name, status, turn, host_user_id, created_at, updated_at, format_version)
                        VALUES (:id, :name, 'LOBBY', 0, :host, :now, :now, :formatVersion)""")
                .param("id", id)
                .param("name", name)
                .param("host", hostUserId)
                .param("now", now)
                .param("formatVersion", formatVersion)
                .update();
        return id;
    }

    public UUID createSandbox(String name, UUID userId, int formatVersion) {
        var id = UUID.randomUUID();
        var now = Timestamp.from(Instant.now());
        jdbc.sql("""
                        INSERT INTO game_bundle (id, name, status, turn, host_user_id, created_at, updated_at, format_version, mode)
                        VALUES (:id, :name, 'LOBBY', 0, :host, :now, :now, :formatVersion, 'SANDBOX')""")
                .param("id", id)
                .param("name", name)
                .param("host", userId)
                .param("now", now)
                .param("formatVersion", formatVersion)
                .update();
        return id;
    }

    public Optional<GameBundle> find(UUID id) {
        return jdbc.sql("SELECT * FROM game_bundle WHERE id = :id")
                .param("id", id)
                .query(GameBundles::map)
                .optional();
    }

    /**
     * Locks the row until the transaction ends, so two users can't change the seats of a lobby at once.
     */
    public Optional<GameBundle> findForUpdate(UUID id) {
        return jdbc.sql("SELECT * FROM game_bundle WHERE id = :id FOR UPDATE")
                .param("id", id)
                .query(GameBundles::map)
                .optional();
    }

    public void updateSummary(UUID id, GameStatus status, int turn, Team winnerTeam, Integer waitingForPlayerNumber) {
        jdbc.sql("""
                        UPDATE game_bundle
                        SET status = :status, turn = :turn, winner_team = :winner, waiting_for_player_number = :waiting,
                            updated_at = :now,
                            end_reason = CASE WHEN :status = 'FINISHED' THEN 'WON' END
                        WHERE id = :id""")
                .param("id", id)
                .param("status", status.name())
                .param("turn", turn)
                .param("winner", winnerTeam == null ? null : winnerTeam.name())
                .param("waiting", waitingForPlayerNumber)
                .param("now", Timestamp.from(Instant.now()))
                .update();
    }

    public void end(UUID id, EndReason reason) {
        jdbc.sql("""
                        UPDATE game_bundle
                        SET status = 'FINISHED', end_reason = :reason, waiting_for_player_number = NULL, updated_at = :now
                        WHERE id = :id""")
                .param("id", id)
                .param("reason", reason.name())
                .param("now", Timestamp.from(Instant.now()))
                .update();
    }

    public void playerCount(UUID id, int playerCount) {
        jdbc.sql("UPDATE game_bundle SET player_count = :count WHERE id = :id")
                .param("id", id)
                .param("count", playerCount)
                .update();
    }

    public void rematch(UUID id, UUID rematchId) {
        jdbc.sql("UPDATE game_bundle SET rematch_id = :rematch WHERE id = :id")
                .param("id", id)
                .param("rematch", rematchId)
                .update();
    }

    public void seed(UUID id, Long seed) {
        jdbc.sql("UPDATE game_bundle SET seed = :seed WHERE id = :id")
                .param("id", id)
                .param("seed", seed)
                .update();
    }

    public static GameBundle map(ResultSet row, int ignored) throws SQLException {
        var winner = row.getString("winner_team");
        var endReason = row.getString("end_reason");
        return new GameBundle(
                row.getObject("id", UUID.class),
                row.getString("name"),
                GameStatus.valueOf(row.getString("status")),
                row.getInt("turn"),
                row.getObject("host_user_id", UUID.class),
                winner == null ? null : Team.valueOf(winner),
                (Integer) row.getObject("waiting_for_player_number"),
                row.getTimestamp("created_at").toInstant(),
                row.getTimestamp("updated_at").toInstant(),
                row.getInt("format_version"),
                GameBundle.GameMode.valueOf(row.getString("mode")),
                (Long) row.getObject("seed"),
                endReason == null ? null : EndReason.valueOf(endReason),
                row.getObject("rematch_id", UUID.class),
                (Integer) row.getObject("player_count"));
    }
}
