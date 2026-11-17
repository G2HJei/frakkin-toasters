package xyz.zlatanov.frakkintoasters.backend.journal;

import tools.jackson.databind.JsonNode;
import xyz.zlatanov.frakkintoasters.projection.GameProjection;
import xyz.zlatanov.frakkintoasters.projection.LiveUpdateProjection;

/**
 * The projections to JSON, in the shapes of the journal's codec: a value of an interface type carries its {@code kind}.
 */
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
}
