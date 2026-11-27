package xyz.zlatanov.frakkintoasters.backend.lobby;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class Participants {

    private final JdbcClient jdbc;

    public record Participant(UUID userId, String name, Integer playerNumber) {

        public boolean isPlayer() {
            return playerNumber != null;
        }
    }

    public List<Participant> of(UUID gameBundleId) {
        return jdbc.sql("""
                        SELECT p.user_id, u.name, p.player_number
                        FROM participant p JOIN app_user u ON u.id = p.user_id
                        WHERE p.game_bundle_id = :game
                        ORDER BY p.player_number NULLS LAST, u.name""")
                .param("game", gameBundleId)
                .query((row, ignored) -> new Participant(row.getObject("user_id", UUID.class), row.getString("name"),
                        (Integer) row.getObject("player_number")))
                .list();
    }

    public Optional<Participant> find(UUID gameBundleId, UUID userId) {
        return of(gameBundleId).stream().filter(participant -> participant.userId().equals(userId)).findFirst();
    }

    public void add(UUID gameBundleId, UUID userId, Integer playerNumber) {
        jdbc.sql("INSERT INTO participant (game_bundle_id, user_id, player_number) VALUES (:game, :user, :seat)")
                .param("game", gameBundleId)
                .param("user", userId)
                .param("seat", playerNumber)
                .update();
    }

    public void remove(UUID gameBundleId, UUID userId) {
        jdbc.sql("DELETE FROM participant WHERE game_bundle_id = :game AND user_id = :user")
                .param("game", gameBundleId)
                .param("user", userId)
                .update();
    }

    public void seat(UUID gameBundleId, UUID userId, Integer playerNumber) {
        jdbc.sql("UPDATE participant SET player_number = :seat WHERE game_bundle_id = :game AND user_id = :user")
                .param("game", gameBundleId)
                .param("user", userId)
                .param("seat", playerNumber)
                .update();
    }
}
