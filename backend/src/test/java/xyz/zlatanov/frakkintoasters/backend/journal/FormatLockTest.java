package xyz.zlatanov.frakkintoasters.backend.journal;

import lombok.val;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Every type name and its components, as the codec writes them, are checked in with the format version. A journal of
 * another shape can't be opened by the codec, so a change of the shape needs a new {@link JournalCodec#FORMAT_VERSION}.
 * After raising it, run {@code ./gradlew :backend:test --tests '*FormatLockTest' -DupdateFormatLock=true} and check in
 * the new lock.
 */
class FormatLockTest {

    private static final Path LOCK = Path.of(System.getProperty("projectDir"), "src/test/resources/journal-format.txt");

    @Test
    void theFormatOnlyChangesWithANewVersion() throws IOException {
        val current = new ArrayList<String>();
        current.add("format version " + JournalCodec.FORMAT_VERSION);
        current.addAll(TypeWalker.fingerprint(TypeRegistry.journal()));

        if (Boolean.getBoolean("updateFormatLock")) {
            Files.write(LOCK, current);
        }
        val locked = Files.exists(LOCK) ? Files.readAllLines(LOCK) : List.<String>of();
        if (locked.isEmpty() || !locked.getFirst().equals(current.getFirst())) {
            fail("The lock is for " + (locked.isEmpty() ? "no version" : locked.getFirst()) + ". Run the test with "
                    + "-DupdateFormatLock=true to lock format version " + JournalCodec.FORMAT_VERSION);
        }
        if (!locked.equals(current)) {
            val added = current.stream().filter(line -> !locked.contains(line)).toList();
            val removed = locked.stream().filter(line -> !current.contains(line)).toList();
            fail("The journal format changed without a new version. Raise JournalCodec.FORMAT_VERSION and run the "
                    + "test with -DupdateFormatLock=true.\nAdded: " + added + "\nRemoved: " + removed);
        }
        assertEquals(locked, current);
    }
}
