package xyz.zlatanov.frakkintoasters.fuzz;

import lombok.val;
import xyz.zlatanov.frakkintoasters.engine.EngineStatus;
import xyz.zlatanov.frakkintoasters.engine.GameEngine;
import xyz.zlatanov.frakkintoasters.engine.PendingInput;
import xyz.zlatanov.frakkintoasters.engine.journal.GameSnapshot;
import xyz.zlatanov.frakkintoasters.state.Game;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeSet;

/**
 * Two engines that have to be in the same game: a restored twin and its original, or one engine before and after an
 * input it rejected.
 */
final class Twins {

    /**
     * What an engine shows: the game as plain values, every player's status, the pending input and the journal's size.
     */
    record State(Map<String, Object> snapshot, List<EngineStatus> statuses, Optional<PendingInput> pending,
                 int journalSize) {

        static State of(GameEngine engine) {
            val statuses = new ArrayList<EngineStatus>();
            for (int player = 1; player <= engine.view().players().size(); player++) {
                statuses.add(engine.status(player));
            }
            return new State(GameSnapshot.of((Game) engine.view()).values(), statuses, engine.pendingInput(),
                    engine.journal().size());
        }
    }

    private Twins() {
    }

    static List<String> differences(GameEngine original, GameEngine twin) {
        return differences(State.of(original), State.of(twin));
    }

    static List<String> differences(State a, State b) {
        val result = new ArrayList<String>();
        val keys = new TreeSet<>(a.snapshot().keySet());
        keys.addAll(b.snapshot().keySet());
        for (val key : keys) {
            if (!Objects.equals(a.snapshot().get(key), b.snapshot().get(key))) {
                result.add(key + ": " + a.snapshot().get(key) + " <> " + b.snapshot().get(key));
            }
        }
        for (int i = 0; i < Math.max(a.statuses().size(), b.statuses().size()); i++) {
            val mine = i < a.statuses().size() ? a.statuses().get(i) : null;
            val theirs = i < b.statuses().size() ? b.statuses().get(i) : null;
            if (!Objects.equals(mine, theirs)) {
                result.add("status " + (i + 1) + ": " + mine + " <> " + theirs);
            }
        }
        if (!a.pending().equals(b.pending())) {
            result.add("pending: " + a.pending() + " <> " + b.pending());
        }
        if (a.journalSize() != b.journalSize()) {
            result.add("journal: " + a.journalSize() + " <> " + b.journalSize() + " entries");
        }
        return result;
    }
}
