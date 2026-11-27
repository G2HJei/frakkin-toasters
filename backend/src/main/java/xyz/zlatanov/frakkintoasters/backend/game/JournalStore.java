package xyz.zlatanov.frakkintoasters.backend.game;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import xyz.zlatanov.frakkintoasters.backend.journal.JournalCodec;
import xyz.zlatanov.frakkintoasters.engine.journal.Journal;
import xyz.zlatanov.frakkintoasters.engine.journal.JournalEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * The primary key (game, sequence) refuses a second writer.
 */
@Repository
@RequiredArgsConstructor
public class JournalStore {

    private final JdbcTemplate jdbc;
    private final JournalCodec codec;

    public void append(UUID gameBundleId, int firstSequence, List<JournalEntry> entries) {
        val rows = new ArrayList<Object[]>();
        for (int i = 0; i < entries.size(); i++) {
            rows.add(new Object[]{gameBundleId, firstSequence + i, codec.encode(entries.get(i))});
        }
        jdbc.batchUpdate("INSERT INTO journal_entry (game_bundle_id, sequence, entry) VALUES (?, ?, ?::jsonb)", rows);
    }

    public Journal load(UUID gameBundleId) {
        val entries = jdbc.query("SELECT entry FROM journal_entry WHERE game_bundle_id = ? ORDER BY sequence",
                (row, ignored) -> codec.decode(row.getString("entry")),
                gameBundleId);
        return new Journal(entries);
    }

    public List<String> loadJson(UUID gameBundleId) {
        return jdbc.queryForList("SELECT entry::text FROM journal_entry WHERE game_bundle_id = ? ORDER BY sequence",
                String.class, gameBundleId);
    }

    public void truncate(UUID gameBundleId, int lastSequence) {
        jdbc.update("DELETE FROM journal_entry WHERE game_bundle_id = ? AND sequence > ?", gameBundleId, lastSequence);
    }
}
