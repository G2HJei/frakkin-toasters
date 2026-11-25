package xyz.zlatanov.frakkintoasters.backend.wire;

import xyz.zlatanov.frakkintoasters.backend.chat.Chat;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.projection.GameProjection;
import xyz.zlatanov.frakkintoasters.projection.GameReport;
import xyz.zlatanov.frakkintoasters.projection.LiveUpdateProjection;

import java.util.List;

/**
 * The messages of the game socket, as records, so the wire schema lists them. They are told apart by {@code type},
 * which is the name in {@link #TYPES}: {@code Messages} and {@code GameSocketHandler} write and read them with these
 * fields.
 */
public final class WireMessages {

    private WireMessages() {
    }

    /**
     * Server to client.
     */
    public sealed interface ServerMessage {
    }

    public record GameMessage(String token, GameProjection game) implements ServerMessage {
    }

    public record UpdateMessage(LiveUpdateProjection update) implements ServerMessage {
    }

    public record LogMessage(LiveUpdateProjection log) implements ServerMessage {
    }

    /**
     * @param reason {@code STALE}, {@code INVALID}, {@code FAILURE} or {@code UNAVAILABLE}
     */
    public record RejectedMessage(String reason, String message) implements ServerMessage {
    }

    public record PongMessage() implements ServerMessage {
    }

    // The ended game told whole, for its results: on a new connection, and with the command that ended it.
    public record ReportMessage(GameReport report) implements ServerMessage {
    }

    // A chat message, to every connection of the game.
    public record ChatMessage(Chat.ChatLine message) implements ServerMessage {
    }

    // The last chat messages, after the log on a new connection.
    public record ChatLogMessage(List<Chat.ChatLine> messages) implements ServerMessage {
    }

    // Why the chat refused the sender's message.
    public record ChatRejectedMessage(String message) implements ServerMessage {
    }

    /**
     * Client to server.
     */
    public sealed interface ClientMessage {
    }

    // The client is there; it pings while it has nothing else to say.
    public record PingMessage() implements ClientMessage {
    }

    // A message in the game's chat, from a player or an observer.
    public record SayMessage(String text) implements ClientMessage {
    }

    public record StatusMessage() implements ClientMessage {
    }

    public record HistoryMessage() implements ClientMessage {
    }

    public record SubmitMessage(String token, Event event) implements ClientMessage {
    }

    public record ChooseMessage(String token, int option) implements ClientMessage {
    }

    public record UndoMessage(String token) implements ClientMessage {
    }

    public record InjectMessage(String token, Event event) implements ClientMessage {
    }

    /**
     * The {@code type} of each message.
     */
    public static final java.util.Map<Class<?>, String> TYPES = java.util.Map.ofEntries(
            java.util.Map.entry(GameMessage.class, "game"),
            java.util.Map.entry(UpdateMessage.class, "update"),
            java.util.Map.entry(LogMessage.class, "log"),
            java.util.Map.entry(RejectedMessage.class, "rejected"),
            java.util.Map.entry(PongMessage.class, "pong"),
            java.util.Map.entry(ReportMessage.class, "report"),
            java.util.Map.entry(ChatMessage.class, "chat"),
            java.util.Map.entry(ChatLogMessage.class, "chatLog"),
            java.util.Map.entry(ChatRejectedMessage.class, "chatRejected"),
            java.util.Map.entry(PingMessage.class, "ping"),
            java.util.Map.entry(SayMessage.class, "say"),
            java.util.Map.entry(StatusMessage.class, "status"),
            java.util.Map.entry(HistoryMessage.class, "history"),
            java.util.Map.entry(SubmitMessage.class, "submit"),
            java.util.Map.entry(ChooseMessage.class, "choose"),
            java.util.Map.entry(UndoMessage.class, "undo"),
            java.util.Map.entry(InjectMessage.class, "inject"));

}
