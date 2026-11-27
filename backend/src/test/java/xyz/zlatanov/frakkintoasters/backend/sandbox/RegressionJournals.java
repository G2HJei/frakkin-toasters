package xyz.zlatanov.frakkintoasters.backend.sandbox;

import lombok.SneakyThrows;
import lombok.val;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.JsonNodeFactory;
import xyz.zlatanov.frakkintoasters.backend.journal.JournalCodec;
import xyz.zlatanov.frakkintoasters.engine.GameEngine;
import xyz.zlatanov.frakkintoasters.engine.journal.Journal;
import xyz.zlatanov.frakkintoasters.engine.journal.JournalEntry;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.fuzz.Repro;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * The journals in {@code src/test/resources/journals}: sandbox exports kept as regression tests. A bug found in the
 * sandbox becomes a test: export the journal, drop it there, and write a test that loads it with {@link #engine} and
 * asserts what should have happened. A journal of the fuzzer also holds the input that failed at its last pause and
 * what that input has to do once the bug is fixed ({@code input} and {@code expect}, see {@link Repro}).
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

    public static GameEngine engine(Path file) {
        return repro(file).restore();
    }

    public static GameEngine engine(String fileName) {
        return engine(DIRECTORY.resolve(fileName));
    }

    @SneakyThrows
    public static Repro repro(Path file) {
        val export = JournalCodec.parse(Files.readString(file));
        if (export.get("formatVersion").intValue() != JournalCodec.FORMAT_VERSION) {
            throw new IllegalArgumentException(file + " is in format " + export.get("formatVersion")
                    + ": it needs an upcaster, or a new export");
        }
        val entries = new ArrayList<JournalEntry>();
        export.get("entries").forEach(entry -> entries.add(CODEC.fromJson(entry)));
        val journal = new Journal(entries);
        val seed = export.get("seed").longValue();
        val input = export.get("input");
        val expect = export.get("expect");
        if (input == null || input.isNull()) {
            return expect == null || expect.isNull()
                    ? Repro.journalOnly(journal, seed)
                    : new Repro(journal, seed, 0, null, null, Repro.Expectation.ofJson(expect.stringValue()));
        }
        val option = present(input.get("option")) ? (Followup) CODEC.values().read(input.get("option"), Followup.class)
                : null;
        val event = present(input.get("event")) ? CODEC.readEvent(input.get("event"), Event.class) : null;
        return new Repro(journal, seed, input.get("player").intValue(), option, event,
                present(expect) ? Repro.Expectation.ofJson(expect.stringValue()) : null);
    }

    @SneakyThrows
    public static void write(Path file, Repro repro, String finding) {
        val export = JsonNodeFactory.instance.objectNode();
        export.put("formatVersion", JournalCodec.FORMAT_VERSION);
        export.put("seed", repro.seed());
        export.put("playerCount", repro.restore().view().players().size());
        if (finding != null) {
            export.put("finding", finding);
        }
        if (repro.hasInput()) {
            val input = export.putObject("input");
            input.put("player", repro.player());
            if (repro.option() != null) {
                input.set("option", CODEC.values().write(repro.option(), Followup.class));
            }
            if (repro.event() != null) {
                input.set("event", CODEC.writeEvent(repro.event()));
            }
        }
        if (repro.expect() != null) {
            export.put("expect", repro.expect().json());
        }
        val entries = export.putArray("entries");
        repro.journal().entries().forEach(entry -> entries.add(CODEC.toJson(entry)));
        Files.writeString(file, JournalCodec.stringify(export));
    }

    private static boolean present(JsonNode node) {
        return node != null && !node.isNull();
    }
}
