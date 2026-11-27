package xyz.zlatanov.frakkintoasters.backend.live;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Component;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;
import xyz.zlatanov.frakkintoasters.backend.chat.Chat.ChatLine;
import xyz.zlatanov.frakkintoasters.backend.game.CommandResult.Rejected;
import xyz.zlatanov.frakkintoasters.backend.game.ProjectionSink.Frames;
import xyz.zlatanov.frakkintoasters.backend.game.ProjectionSink.GameMessage;
import xyz.zlatanov.frakkintoasters.backend.journal.JournalCodec;
import xyz.zlatanov.frakkintoasters.backend.journal.WireCodec;
import xyz.zlatanov.frakkintoasters.projection.GameReport;
import xyz.zlatanov.frakkintoasters.projection.LiveUpdateProjection;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class Messages {

    private static final JsonNodeFactory NODES = JsonNodeFactory.instance;

    private final WireCodec codec;

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
        if (frames.report() != null) {
            messages.add(report(frames.report()));
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

    public String report(GameReport report) {
        val message = NODES.objectNode();
        message.put("type", "report");
        message.set("report", codec.report(report));
        return JournalCodec.stringify(message);
    }

    public String log(LiveUpdateProjection log) {
        val message = NODES.objectNode();
        message.put("type", "log");
        message.set("log", codec.update(log));
        return JournalCodec.stringify(message);
    }

    public String chat(ChatLine line) {
        val message = NODES.objectNode();
        message.put("type", "chat");
        message.set("message", line(line));
        return JournalCodec.stringify(message);
    }

    public String chatLog(List<ChatLine> lines) {
        val message = NODES.objectNode();
        message.put("type", "chatLog");
        val array = message.putArray("messages");
        lines.forEach(line -> array.add(line(line)));
        return JournalCodec.stringify(message);
    }

    public String chatRejected(String text) {
        val message = NODES.objectNode();
        message.put("type", "chatRejected");
        message.put("message", text);
        return JournalCodec.stringify(message);
    }

    private static ObjectNode line(ChatLine line) {
        val node = NODES.objectNode();
        node.put("id", line.id());
        node.put("userId", line.userId());
        node.put("name", line.name());
        if (line.playerNumber() == null) {
            node.putNull("playerNumber");
        } else {
            node.put("playerNumber", line.playerNumber());
        }
        node.put("text", line.text());
        node.put("at", line.at());
        return node;
    }

    public String pong() {
        val message = NODES.objectNode();
        message.put("type", "pong");
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
