package xyz.zlatanov.frakkintoasters.backend.sandbox;

import lombok.val;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.io.TempDir;
import xyz.zlatanov.frakkintoasters.engine.ScriptedTurns;
import xyz.zlatanov.frakkintoasters.engine.journal.GameSnapshot;
import xyz.zlatanov.frakkintoasters.engine.journal.Journal;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.fuzz.Repro;
import xyz.zlatanov.frakkintoasters.state.Game;

import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.fuzz.Repro.Expectation.ANSWERS;
import static xyz.zlatanov.frakkintoasters.fuzz.Repro.Expectation.RUNS;

class RegressionJournalsTest {

    @TestFactory
    Stream<DynamicTest> everyJournalRestores() throws IOException {
        if (Boolean.getBoolean("writeExampleJournal")) {
            writeExample();
        }
        val files = RegressionJournals.files();
        assertFalse(files.isEmpty(), "No journals in " + RegressionJournals.DIRECTORY);
        return files.stream().map(file -> DynamicTest.dynamicTest(file.getFileName().toString(),
                () -> RegressionJournals.engine(file)));
    }

    @TestFactory
    Stream<DynamicTest> everyJournalInputDoesWhatItExpects() {
        record Exported(Path file, Repro repro) {
        }
        return RegressionJournals.files().stream()
                .map(file -> new Exported(file, RegressionJournals.repro(file)))
                .filter(exported -> exported.repro().expect() != null)
                .map(exported -> DynamicTest.dynamicTest(
                        exported.file().getFileName() + " " + exported.repro().expect().json(), exported.repro()::check));
    }

    @Test
    void theScriptedTurnsEndInTurn5() {
        val engine = RegressionJournals.engine("scripted-turns.json");

        assertEquals(5, engine.view().turn().number());
    }

    @Test
    void aJournalWithItsLastInputGoesThroughWriteReadAndApply(@TempDir Path folder) {
        val engine = ScriptedTurns.start();
        val inputs = ScriptedTurns.inputs();
        inputs.subList(0, inputs.size() - 1).forEach(input -> input.apply(engine.view()).applyTo(engine));
        val last = inputs.getLast().apply(engine.view());
        val file = folder.resolve("last-input.json");

        RegressionJournals.write(file, new Repro(engine.journal(), ScriptedTurns.SEED, last.player(), null,
                last.submit(), RUNS), "an example");
        val read = RegressionJournals.repro(file);

        assertEquals(last.player(), read.player());
        assertEquals(last.submit(), read.event());
        assertNull(read.option());
        assertEquals(RUNS, read.expect());
        read.check();
        val played = read.restore();
        read.applyTo(played);
        assertEquals(GameSnapshot.of((Game) ScriptedTurns.play().view()), GameSnapshot.of((Game) played.view()));
    }

    @Test
    void anOptionGoesThroughWriteAndRead(@TempDir Path folder) {
        val engine = ScriptedTurns.start();
        val inputs = ScriptedTurns.inputs();
        // the 12th input picks the top of "Riots"
        inputs.subList(0, 11).forEach(input -> input.apply(engine.view()).applyTo(engine));
        val choice = inputs.get(11).apply(engine.view());
        val file = folder.resolve("option.json");

        RegressionJournals.write(file, new Repro(engine.journal(), ScriptedTurns.SEED, choice.player(),
                choice.choose(), null, ANSWERS), null);
        val read = RegressionJournals.repro(file);

        assertEquals(choice.choose(), read.option());
        assertNull(read.event());
        read.check();
    }

    @Test
    void aRejectedInputThatChangesNothingRuns(@TempDir Path folder) {
        val engine = ScriptedTurns.play();
        val file = folder.resolve("rejected.json");

        // nobody is asked to choose at the Receive Skills step
        RegressionJournals.write(file, new Repro(engine.journal(), ScriptedTurns.SEED, 1, single(new NoOpEvent(1)), null,
                RUNS), null);

        RegressionJournals.repro(file).check();
    }

    @Test
    void aJournalWithoutAnInputIsOnlyRestored(@TempDir Path folder) {
        val file = folder.resolve("journal-only.json");

        RegressionJournals.write(file, Repro.journalOnly(ScriptedTurns.play().journal(), ScriptedTurns.SEED), null);
        val read = RegressionJournals.repro(file);

        assertFalse(read.hasInput());
        assertNull(read.expect());
        assertEquals(5, read.restore().view().turn().number());
    }

    // Written like a sandbox export, so a test of a real sandbox's journal looks the same.
    private static void writeExample() {
        RegressionJournals.write(RegressionJournals.DIRECTORY.resolve("scripted-turns.json"),
                Repro.journalOnly(new Journal(ScriptedTurns.play().journal().entries()), ScriptedTurns.SEED), null);
    }
}
