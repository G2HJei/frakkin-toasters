package xyz.zlatanov.frakkintoasters.backend.journal;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.engine.GameEngine;
import xyz.zlatanov.frakkintoasters.engine.ScriptedTurns;
import xyz.zlatanov.frakkintoasters.projection.GameProjection;
import xyz.zlatanov.frakkintoasters.projection.LiveUpdateProjection;
import xyz.zlatanov.frakkintoasters.projection.Viewer;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WireCodecTest {

    private final WireCodec codec = new WireCodec();

    @Test
    void everyProjectionOfASeededGameGoesToJson() {
        val engine = ScriptedTurns.start();
        var from = 0;
        assertWritesEverything(engine, from);
        for (val input : ScriptedTurns.inputs()) {
            from = engine.journal().size();
            input.apply(engine.view()).applyTo(engine);
            assertWritesEverything(engine, from);
        }
    }

    @Test
    void aProjectionNamesTheTypesOfItsInterfaces() {
        val engine = ScriptedTurns.start();
        val first = engine.pendingInput().orElseThrow().player();

        val json = JournalCodec.stringify(codec.game(GameProjection.of(engine.view(), engine.pendingInput(),
                Viewer.player(first))));

        assertTrue(json.contains("\"viewer\":{\"kind\":\"PlayerViewer\",\"playerNumber\":" + first + "}"), json);
        assertTrue(json.contains("\"input\":{\"kind\":\"Decide\",\"player\":" + first
                + ",\"action\":\"SelectCharacterEvent\",\"constraints\":[]}"), json);
    }

    private void assertWritesEverything(GameEngine engine, int from) {
        for (val viewer : List.of(Viewer.player(1), Viewer.player(2), Viewer.player(3), Viewer.observer())) {
            val game = codec.game(GameProjection.of(engine.view(), engine.pendingInput(), viewer));
            assertEquals(viewer instanceof Viewer.Observer ? "Observer" : "PlayerViewer",
                    game.get("viewer").get(ValueCodec.KIND).stringValue());
            codec.update(LiveUpdateProjection.of(engine.journal().entries(), from, viewer));
        }
    }
}
