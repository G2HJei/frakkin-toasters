package xyz.zlatanov.frakkintoasters.backend.sandbox;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import xyz.zlatanov.frakkintoasters.fuzz.Finding;
import xyz.zlatanov.frakkintoasters.fuzz.Playout;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Turns a finding of the fuzzer into a regression journal: {@code ./gradlew :backend:test --tests
 * '*FuzzJournalExportTest' -DfuzzJournal=4:117} plays seed 117 with 4 players and writes
 * {@code journals/fuzz-4p-117.json}: the journal at the pause before the finding, the input that failed there and what
 * it has to do once the bug is fixed. The finding is the one that stopped the game, else the game's first; add its ID
 * to pick another ({@code 4:117:F-1c2d3e}). It runs only when asked, like {@code -DwriteExampleJournal}.
 */
@EnabledIfSystemProperty(named = "fuzzJournal", matches = ".+")
class FuzzJournalExportTest {

    @Test
    void writesTheFindingAsARegressionJournal() {
        val parts = System.getProperty("fuzzJournal").split(":");
        val players = Integer.parseInt(parts[0].strip());
        val seed = Long.parseLong(parts[1].strip());
        val result = Playout.play(players, seed, Playout.Options.defaults().withRepro());
        val finding = parts.length > 2
                ? result.findings().stream().filter(f -> f.id().equals(parts[2].strip())).findFirst().orElse(null)
                : result.stoppedBy() != null ? result.stoppedBy() : result.findings().stream().findFirst().orElse(null);
        assertNotNull(finding, "No such finding in " + players + "p seed " + seed + ": " + result.findings().stream()
                .map(Finding::id).toList());
        val repro = finding.pause() == null ? null : finding.pause().repro();
        assertNotNull(repro, finding.id() + " has no journal: it came before the game started");
        val file = RegressionJournals.DIRECTORY.resolve("fuzz-" + players + "p-" + seed + ".json");

        RegressionJournals.write(file, repro, finding.id() + " " + finding.kind() + ": " + finding.summary());

        val written = RegressionJournals.repro(file);
        if (written.expect() != null) {
            // Until the fix, RegressionJournalsTest fails on it.
            assertThrows(AssertionError.class, written::check, file + " doesn't reproduce " + finding.id());
        }
        System.out.println("Wrote " + file + " for " + finding.id() + " " + finding.kind() + ": " + finding.summary());
    }
}
