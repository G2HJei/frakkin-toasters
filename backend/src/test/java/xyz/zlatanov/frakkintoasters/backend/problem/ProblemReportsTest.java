package xyz.zlatanov.frakkintoasters.backend.problem;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.server.ResponseStatusException;
import xyz.zlatanov.frakkintoasters.backend.BackendTest;
import xyz.zlatanov.frakkintoasters.backend.lobby.Lobby;
import xyz.zlatanov.frakkintoasters.backend.lobby.Lobby.Role;
import xyz.zlatanov.frakkintoasters.backend.problem.ProblemReports.Problem;
import xyz.zlatanov.frakkintoasters.backend.user.AppUserAuthentication.AppUserPrincipal;

import static org.junit.jupiter.api.Assertions.*;

class ProblemReportsTest extends BackendTest {

    @Autowired
    ProblemReports problems;
    @Autowired
    Lobby          lobby;

    @Test
    void aPlayerReportsAProblemWithWhereTheGameStood() {
        val host = createUser("Adama");
        val room = lobby.host(host, "Galactica");
        lobby.join(room.id(), createUser("Roslin"), Role.PLAYER);
        lobby.join(room.id(), createUser("Starbuck"), Role.PLAYER);
        lobby.start(room.id(), host);

        val id = problems.report(new AppUserPrincipal(host), room.id(), new Problem("  The die rolled twice  ")).get("id");

        val stored = jdbc.sql("SELECT text, sequence FROM problem_report WHERE id = :id")
                .param("id", id)
                .query((row, ignored) -> new Object[]{row.getString("text"), row.getInt("sequence")})
                .single();
        assertEquals("The die rolled twice", stored[0]);
        val journal = jdbc.sql("SELECT max(sequence) FROM journal_entry WHERE game_bundle_id = :game")
                .param("game", room.id())
                .query(Integer.class)
                .single();
        assertEquals(journal, stored[1]);
    }

    @Test
    void aReportIsShortAndOnlyFromSomeoneAtTheTable() {
        val host = createUser("Cain");
        val room = lobby.host(host, "Pegasus");
        val me = new AppUserPrincipal(host);

        assertThrows(ResponseStatusException.class, () -> problems.report(me, room.id(), new Problem(" ")));
        assertThrows(ResponseStatusException.class,
                () -> problems.report(me, room.id(), new Problem("x".repeat(ProblemReports.MAX_LENGTH + 1))));
        assertThrows(ResponseStatusException.class,
                () -> problems.report(new AppUserPrincipal(createUser("Stranger")), room.id(), new Problem("Hi")));
        assertNotNull(problems.report(me, room.id(), new Problem("x".repeat(ProblemReports.MAX_LENGTH))).get("id"));
    }
}
