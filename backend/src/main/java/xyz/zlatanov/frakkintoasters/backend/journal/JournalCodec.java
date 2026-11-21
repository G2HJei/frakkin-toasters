package xyz.zlatanov.frakkintoasters.backend.journal;

import lombok.val;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import xyz.zlatanov.frakkintoasters.engine.journal.JournalEntry;
import xyz.zlatanov.frakkintoasters.event.Event;

/**
 * A {@link JournalEntry} to JSON and back. What comes back equals what was written, because the agenda replay compares
 * events with {@code equals}.
 */
public final class JournalCodec {

    /**
     * The version of the JSON this codec writes. A game written by another version can't be opened. The format lock test
     * fails when a type changes without a new version.
     */
    public static final int FORMAT_VERSION = 19;

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private final ValueCodec codec = new ValueCodec(TypeRegistry.journal());

    public ValueCodec values() {
        return codec;
    }

    public JsonNode toJson(JournalEntry entry) {
        return codec.write(entry, JournalEntry.class);
    }

    public JournalEntry fromJson(JsonNode node) {
        return (JournalEntry) codec.read(node, JournalEntry.class);
    }

    public String encode(JournalEntry entry) {
        return JSON.writeValueAsString(toJson(entry));
    }

    public JournalEntry decode(String json) {
        return fromJson(parse(json));
    }

    public JsonNode writeEvent(Event event) {
        return codec.write(event, Event.class);
    }

    /**
     * @param expected the type the event has to be
     */
    public <E extends Event> E readEvent(JsonNode node, Class<E> expected) {
        val event = codec.read(node, Event.class);
        if (!expected.isInstance(event)) {
            throw new CodecException("Not a " + expected.getSimpleName() + ": " + node.get(ValueCodec.KIND));
        }
        return expected.cast(event);
    }

    public static JsonNode parse(String json) {
        try {
            return JSON.readTree(json);
        } catch (JacksonException e) {
            throw new CodecException("Not JSON: " + e.getOriginalMessage(), e);
        }
    }

    public static String stringify(JsonNode node) {
        return JSON.writeValueAsString(node);
    }
}
