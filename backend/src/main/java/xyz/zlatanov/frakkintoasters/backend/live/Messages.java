package xyz.zlatanov.frakkintoasters.backend.live;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Component;
import tools.jackson.databind.node.JsonNodeFactory;
import xyz.zlatanov.frakkintoasters.backend.game.CommandResult.Rejected;
import xyz.zlatanov.frakkintoasters.backend.game.ProjectionSink.Frames;
import xyz.zlatanov.frakkintoasters.backend.game.ProjectionSink.GameMessage;
import xyz.zlatanov.frakkintoasters.backend.journal.JournalCodec;
import xyz.zlatanov.frakkintoasters.backend.journal.WireCodec;
import xyz.zlatanov.frakkintoasters.projection.LiveUpdateProjection;

import java.util.ArrayList;
import java.util.List;

/**
 * The messages the server sends, as JSON with a {@code type}:
 * <ul>
 *     <li>{@code game}: the token and a {@code GameProjection}</li>
 *     <li>{@code update}: a {@code LiveUpdateProjection} of what a command did</li>
 *     <li>{@code log}: the {@code LiveUpdateProjection} of the whole journal, which replaces the client's log</li>
 *     <li>{@code rejected}: why a command was refused: stale, invalid (the engine's message), or a server failure</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
public class Messages {

    private static final JsonNodeFactory NODES = JsonNodeFactory.instance;

    private final WireCodec codec;

    /**
     * @return the messages of the frames in the order they go: a command's update before its game, and the whole log
     * after the game, as a new connection gets them
     */
    public List<String> of(Frames frames) {
        val messages = new ArrayList<String>();
        if (frames.update() != null) {
            messages.add(update(frames.update()));
        }
        if (frames.game() != null) {
            messages.add(game(frames.game()));
        }
        if (frames.log() != null) {
            messages.add(log(frames.log()));
        }
        return messages;
    }

    public String game(GameMessage game) {
        val message = NODES.objectNode();
        message.put("type", "game");
        message.put("token", game.token());
        message.set("game", codec.game(game.game()));
        return JournalCodec.stringify(message);
    }

    public String update(LiveUpdateProjection update) {
        val message = NODES.objectNode();
        message.put("type", "update");
        message.set("update", codec.update(update));
        return JournalCodec.stringify(message);
    }

    public String log(LiveUpdateProjection log) {
        val message = NODES.objectNode();
        message.put("type", "log");
        message.set("log", codec.update(log));
        return JournalCodec.stringify(message);
    }

    public String rejected(Rejected rejected) {
        return rejected(rejected.reason().name(), rejected.message());
    }

    public String rejected(String reason, String text) {
        val message = NODES.objectNode();
        message.put("type", "rejected");
        message.put("reason", reason);
        message.put("message", text);
        return JournalCodec.stringify(message);
    }
}
