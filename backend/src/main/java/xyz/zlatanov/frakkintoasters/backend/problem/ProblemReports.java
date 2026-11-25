package xyz.zlatanov.frakkintoasters.backend.problem;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import xyz.zlatanov.frakkintoasters.backend.game.GameBundle.GameStatus;
import xyz.zlatanov.frakkintoasters.backend.game.GameBundles;
import xyz.zlatanov.frakkintoasters.backend.lobby.Participants;
import xyz.zlatanov.frakkintoasters.backend.user.AppUserAuthentication.AppUserPrincipal;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * "Report a problem" from a game's table: a user's words about the game, stored with the game and the journal's last
 * sequence, so the report can be looked at where the game stood (a bug report with the journal is milestone E).
 * Whoever can open the table may report: a participant, or anyone watching a finished game.
 */
@RestController
@RequiredArgsConstructor
public class ProblemReports {

    public static final int MAX_LENGTH  = 1000;
    static final int        MAX_PER_GAME = 20;

    private final JdbcClient   jdbc;
    private final GameBundles  bundles;
    private final Participants participants;

    public record Problem(String text) {
    }

    @PostMapping("/api/games/{id}/problems")
    public Map<String, Long> report(@AuthenticationPrincipal AppUserPrincipal user, @PathVariable UUID id,
                                    @RequestBody Problem problem) {
        val bundle = bundles.find(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No such game"));
        if (participants.find(id, user.id()).isEmpty() && bundle.status() != GameStatus.FINISHED) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You aren't in this game");
        }
        val text = problem.text() == null ? "" : problem.text().strip();
        if (text.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Say what went wrong");
        }
        if (text.length() > MAX_LENGTH) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A report has at most " + MAX_LENGTH + " characters");
        }
        val reported = jdbc.sql("SELECT count(*) FROM problem_report WHERE game_bundle_id = :game AND user_id = :user")
                .param("game", id)
                .param("user", user.id())
                .query(Long.class)
                .single();
        if (reported >= MAX_PER_GAME) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "You reported " + MAX_PER_GAME
                    + " problems in this game already");
        }
        val reportId = jdbc.sql("""
                        INSERT INTO problem_report (game_bundle_id, user_id, sequence, text, created_at)
                        VALUES (:game, :user,
                                (SELECT coalesce(max(sequence), 0) FROM journal_entry WHERE game_bundle_id = :game),
                                :text, :now)
                        RETURNING id""")
                .param("game", id)
                .param("user", user.id())
                .param("text", text)
                .param("now", Timestamp.from(Instant.now()))
                .query(Long.class)
                .single();
        return Map.of("id", reportId);
    }
}
