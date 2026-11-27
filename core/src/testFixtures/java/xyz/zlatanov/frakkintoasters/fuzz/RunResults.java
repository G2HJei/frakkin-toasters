package xyz.zlatanov.frakkintoasters.fuzz;

import lombok.val;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * The games of a run as they come in from the threads. Each game is kept slim; only the example game of each finding
 * keeps its inputs and pauses, so a run of 10,000 games fits in memory.
 */
final class RunResults {

    static final Comparator<PlayoutResult> BY_GAME = Comparator.comparingInt(PlayoutResult::players)
            .thenComparingLong(PlayoutResult::seed);

    private final List<PlayoutResult>        games      = new ArrayList<>();
    private final Set<String>                eventTypes = new TreeSet<>();
    private final Map<String, PlayoutResult> examples   = new HashMap<>();

    synchronized void add(PlayoutResult result) {
        eventTypes.addAll(result.eventTypes());
        for (val finding : result.findings()) {
            examples.merge(finding.id(), result, (kept, other) -> BY_GAME.compare(kept, other) <= 0 ? kept : other);
        }
        games.add(result.slim());
    }

    synchronized List<PlayoutResult> games() {
        return games.stream().sorted(BY_GAME).toList();
    }

    synchronized Set<String> eventTypes() {
        return Set.copyOf(eventTypes);
    }

    /**
     * @return the game with the lowest player count and seed that showed the finding, with its pause
     */
    synchronized PlayoutResult example(String findingId) {
        return examples.get(findingId);
    }

    synchronized Finding exampleFinding(String findingId) {
        return examples.get(findingId).findings().stream()
                .filter(finding -> finding.id().equals(findingId))
                .findFirst()
                .orElseThrow();
    }

    synchronized List<Finding> findings() {
        return examples.keySet().stream().sorted().map(this::exampleFinding).toList();
    }

    synchronized int failed() {
        return (int) games.stream().filter(PlayoutResult::failed).count();
    }
}
