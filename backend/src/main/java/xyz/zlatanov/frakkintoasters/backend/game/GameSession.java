package xyz.zlatanov.frakkintoasters.backend.game;

import xyz.zlatanov.frakkintoasters.engine.GameEngine;
import xyz.zlatanov.frakkintoasters.engine.journal.Journal;
import xyz.zlatanov.frakkintoasters.projection.GameProjection;
import xyz.zlatanov.frakkintoasters.projection.GameReport;
import xyz.zlatanov.frakkintoasters.projection.Viewer;
import xyz.zlatanov.frakkintoasters.state.view.GameView;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * It asks the engine for the journal and the view on every use, because a rollback replaces both.
 */
public final class GameSession {

    private final UUID       gameBundleId;
    private final GameEngine engine;

    // The last game each viewer was sent after a command, so a viewer for whom nothing changed gets nothing.
    private final Map<Viewer, GameProjection> published = new HashMap<>();

    private boolean closed;

    // the report of the ended game, and the sequence it was read at: an undo can take the ending back
    private GameReport report;
    private int        reportedAt = -1;

    GameSession(UUID gameBundleId, GameEngine engine) {
        this.gameBundleId = gameBundleId;
        this.engine = engine;
    }

    public UUID gameBundleId() {
        return gameBundleId;
    }

    public GameEngine engine() {
        return engine;
    }

    public Journal journal() {
        return engine.journal();
    }

    public GameView view() {
        return engine.view();
    }

    public int sequence() {
        return engine.journal().size();
    }

    public Optional<GameReport> report() {
        if (engine.view().outcome().isEmpty()) {
            return Optional.empty();
        }
        if (reportedAt != sequence()) {
            report = GameReport.of(journal().entries()).orElse(null);
            reportedAt = sequence();
        }
        return Optional.ofNullable(report);
    }

    public boolean closed() {
        return closed;
    }

    void close() {
        closed = true;
    }

    GameProjection publish(Viewer viewer, GameProjection game) {
        return published.put(viewer, game);
    }

    void forgetPublished() {
        published.clear();
    }
}
