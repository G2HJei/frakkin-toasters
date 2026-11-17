package xyz.zlatanov.frakkintoasters.backend.game;

import xyz.zlatanov.frakkintoasters.engine.GameEngine;
import xyz.zlatanov.frakkintoasters.engine.journal.Journal;
import xyz.zlatanov.frakkintoasters.projection.GameProjection;
import xyz.zlatanov.frakkintoasters.projection.Viewer;
import xyz.zlatanov.frakkintoasters.state.view.GameView;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * A game in memory. It asks the engine for the journal and the view on every use, because a rollback replaces both.
 */
public final class GameSession {

    private final UUID       gameBundleId;
    private final GameEngine engine;

    // The last game each viewer was sent after a command, so a viewer for whom nothing changed gets nothing.
    private final Map<Viewer, GameProjection> published = new HashMap<>();

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

    /**
     * @return the number of journal entries, which names the state of the game
     */
    public int sequence() {
        return engine.journal().size();
    }

    GameProjection publish(Viewer viewer, GameProjection game) {
        return published.put(viewer, game);
    }

    void forgetPublished() {
        published.clear();
    }
}
