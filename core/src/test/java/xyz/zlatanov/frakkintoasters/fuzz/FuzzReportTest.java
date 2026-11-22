package xyz.zlatanov.frakkintoasters.fuzz;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import xyz.zlatanov.frakkintoasters.fuzz.Endings.Ending;
import xyz.zlatanov.frakkintoasters.fuzz.Finding.Kind;
import xyz.zlatanov.frakkintoasters.fuzz.Finding.Pause;
import xyz.zlatanov.frakkintoasters.state.Team;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class FuzzReportTest {

    private static final Finding CRASH = Finding.of(Kind.CRASH, "NullPointerException in X.isValid on XEvent",
                    "`NullPointerException` in `X.isValid` on `XEvent`")
            .at(new Pause("Turn 3, `ACTION`, player 2 asked for `ActionEvent`", "pending", List.of("t3 ACTION p2"), null));
    private static final Finding DEAD  = Finding.of(Kind.DEAD_OPTION, "decision YEvent",
                    "An option leads to `YEvent`, which has no legal answer")
            .at(new Pause("Turn 1, `MOVEMENT`, player 1 chooses among 2 options", "pending", List.of(), null));

    private static final FuzzReport.Run RUN = new FuzzReport.Run(LocalDateTime.of(2026, 11, 24, 10, 12),
            Duration.ofSeconds(370), "abc1234", 3, 1, 2, "3-4", 50, "core/build/fuzz");

    private static final String EXISTING = """
            # Fuzzer report

            Written by hand above the block.

            <!-- fuzz:begin -->

            ## Last run

            An older run.

            ### History

            | Run | Commit | Games | Failed | Open findings |
            |---|---|---|---|---|
            | 2026-11-23 09:00 | `0000000` | 10 | 1 | 2 |

            <!-- fuzz:end -->

            ## Fixes

            ### %s - CRASH - fixed 2026-11-23

            - Cause: `isValid` read a null card.

            ## Questions

            - **%s** (DEAD_OPTION): every player is asked. Recommendation: ask only the holder.
            """.formatted(CRASH.id(), DEAD.id());

    @Test
    void theHandWrittenPartsStay(@TempDir Path folder) throws Exception {
        val report = folder.resolve("fuzzer-report.md");
        Files.writeString(report, EXISTING);

        FuzzReport.write(report, RUN, results());

        val written = Files.readString(report);
        assertTrue(written.startsWith("# Fuzzer report\n\nWritten by hand above the block.\n\n<!-- fuzz:begin -->"));
        assertTrue(written.endsWith(EXISTING.substring(EXISTING.indexOf("<!-- fuzz:end -->"))), written);
        assertFalse(written.contains("An older run."));
    }

    @Test
    void theHistoryKeepsItsRowsUnderTheNewOne() {
        val rendered = FuzzReport.render(EXISTING, RUN, results());

        val newRow = rendered.indexOf("| 2026-11-24 10:12 | `abc1234` | 3 | 1 | 1 |");
        val oldRow = rendered.indexOf("| 2026-11-23 09:00 | `0000000` | 10 | 1 | 2 |");
        assertTrue(newRow > 0 && oldRow > newRow, rendered);
        assertEquals(2, FuzzReport.historyRows(rendered).size());
    }

    // A fixed finding seen again is regressed, one under "Questions" is a question, and a question isn't open.
    @Test
    void aFixedFindingSeenAgainIsRegressed() {
        val rendered = FuzzReport.render(EXISTING, RUN, results());

        assertTrue(rendered.contains("| " + CRASH.id() + " | CRASH | `NullPointerException` in `X.isValid` on `XEvent` | 1"
                + " | 3p seed 2 | regressed |"), rendered);
        assertTrue(rendered.contains("| " + DEAD.id() + " | DEAD_OPTION | An option leads to `YEvent`, which has no"
                + " legal answer | 2 | 3p seed 1 | question |"), rendered);
    }

    @Test
    void aNewReportHasEverySection() {
        val rendered = FuzzReport.render(null, RUN, results());

        for (val section : List.of("# Fuzzer report", "<!-- fuzz:begin -->", "## Last run", "### Endings",
                "### Findings", "### Watch", "Event types that never ran", "### History", "<!-- fuzz:end -->",
                "## Fixes", "## Questions")) {
            assertTrue(rendered.contains(section), section + " in\n" + rendered);
        }
        assertTrue(rendered.contains("2026-11-24 10:12, `abc1234` (3 changed files), seeds 1-2, 3-4 players, 6m 10s."));
        assertTrue(rendered.contains("| 3 | 2 | 1 | 1 | 1 | 0 | 10 / 10 |"), rendered);
        assertTrue(rendered.contains("| Kobol, the humans win | 1 | 0 |"), rendered);
        assertTrue(rendered.contains("- **" + DEAD.id() + "**: `./gradlew :core:fuzz -Pplayers=3 -Pseed=1`, trace"
                + " `core/build/fuzz/" + DEAD.id() + "-3p-1.txt`. Turn 1, `MOVEMENT`, player 1 chooses among 2 options."),
                rendered);
        assertTrue(rendered.contains("- 4 players: the humans never won in 1 game."), rendered);
        assertTrue(rendered.contains("| " + CRASH.id() + " | CRASH |") && rendered.contains("| open |"), rendered);
    }

    @Test
    void aSecondRunKeepsTheFirstInTheHistory() {
        val first = FuzzReport.render(null, RUN, results());

        val second = FuzzReport.render(first, RUN, results());

        assertEquals(2, FuzzReport.historyRows(second).size());
        assertEquals(1, second.split("## Fixes", -1).length - 1);
    }

    private static RunResults results() {
        val results = new RunResults();
        results.add(game(3, 1, Team.HUMANS, Ending.KOBOL_HUMANS, null, DEAD));
        results.add(game(3, 2, null, null, CRASH, DEAD, CRASH));
        results.add(game(4, 1, Team.CYLONS, Ending.FOOD_AT_0, null));
        return results;
    }

    private static PlayoutResult game(int players, long seed, Team winner, Ending ending, Finding stoppedBy,
                                      Finding... findings) {
        return new PlayoutResult(players, seed, Optional.ofNullable(winner), Optional.ofNullable(ending), 10, 500,
                stoppedBy, List.of(findings), List.of("t1 MOVEMENT p1"), Set.of("StartGameEvent"));
    }
}
