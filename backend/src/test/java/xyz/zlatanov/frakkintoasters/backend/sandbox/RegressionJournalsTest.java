package xyz.zlatanov.frakkintoasters.backend.sandbox;

import lombok.val;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import tools.jackson.databind.node.JsonNodeFactory;
import xyz.zlatanov.frakkintoasters.backend.journal.JournalCodec;
import xyz.zlatanov.frakkintoasters.engine.ScriptedTurns;

import java.io.IOException;
import java.nio.file.Files;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

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

    // The scripted turns as an example: they end at the second player's Receive Skills step of turn 5.
    @Test
    void theScriptedTurnsEndInTurn5() {
        val engine = RegressionJournals.engine("scripted-turns.json");

        assertEquals(5, engine.view().turn().number());
    }

    // Written like a sandbox export, so a test of a real sandbox's journal looks the same.
    private static void writeExample() throws IOException {
        val codec = new JournalCodec();
        val engine = ScriptedTurns.play();
        val export = JsonNodeFactory.instance.objectNode();
        export.put("formatVersion", JournalCodec.FORMAT_VERSION);
        export.put("seed", ScriptedTurns.SEED);
        export.put("playerCount", 3);
        val entries = export.putArray("entries");
        engine.journal().entries().forEach(entry -> entries.add(codec.toJson(entry)));
        Files.writeString(RegressionJournals.DIRECTORY.resolve("scripted-turns.json"), JournalCodec.stringify(export));
    }
}
