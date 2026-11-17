package xyz.zlatanov.frakkintoasters.backend.sandbox;

import lombok.SneakyThrows;
import lombok.val;
import xyz.zlatanov.frakkintoasters.backend.journal.JournalCodec;
import xyz.zlatanov.frakkintoasters.engine.GameEngine;
import xyz.zlatanov.frakkintoasters.engine.journal.Journal;
import xyz.zlatanov.frakkintoasters.engine.journal.JournalEntry;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * The journals in {@code src/test/resources/journals}: sandbox exports kept as regression tests. A bug found in the
 * sandbox becomes a test: export the journal, drop it there, and write a test that loads it with {@link #engine} and
 * asserts what should have happened.
 */
public final class RegressionJournals {

    public static final Path DIRECTORY = Path.of(System.getProperty("projectDir"), "src/test/resources/journals");

    private static final JournalCodec CODEC = new JournalCodec();

    private RegressionJournals() {
    }

    @SneakyThrows
    public static List<Path> files() {
        try (Stream<Path> files = Files.list(DIRECTORY)) {
            return files.filter(file -> file.toString().endsWith(".json")).sorted().toList();
        }
    }

    /**
     * @return the game of the exported journal, restored with its seed, so it goes on as the sandbox would
     */
    @SneakyThrows
    public static GameEngine engine(Path file) {
        val export = JournalCodec.parse(Files.readString(file));
        if (export.get("formatVersion").intValue() != JournalCodec.FORMAT_VERSION) {
            throw new IllegalArgumentException(file + " is in format " + export.get("formatVersion")
                    + ": it needs an upcaster, or a new export");
        }
        val entries = new ArrayList<JournalEntry>();
        export.get("entries").forEach(entry -> entries.add(CODEC.fromJson(entry)));
        return GameEngine.restore(new Journal(entries), export.get("seed").longValue());
    }

    public static GameEngine engine(String fileName) {
        return engine(DIRECTORY.resolve(fileName));
    }
}
