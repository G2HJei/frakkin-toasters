package xyz.zlatanov.frakkintoasters.backend.journal;

import tools.jackson.databind.JsonNode;
import xyz.zlatanov.frakkintoasters.projection.GameProjection;
import xyz.zlatanov.frakkintoasters.projection.GameReport;
import xyz.zlatanov.frakkintoasters.projection.LiveUpdateProjection;

public final class WireCodec {

    private final ValueCodec codec = new ValueCodec(TypeRegistry.wire());

    public ValueCodec values() {
        return codec;
    }

    public JsonNode game(GameProjection game) {
        return codec.write(game, GameProjection.class);
    }

    public JsonNode update(LiveUpdateProjection update) {
        return codec.write(update, LiveUpdateProjection.class);
    }

    public JsonNode report(GameReport report) {
        return codec.write(report, GameReport.class);
    }
}
