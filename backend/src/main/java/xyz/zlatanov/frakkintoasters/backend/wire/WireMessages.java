package xyz.zlatanov.frakkintoasters.backend.wire;

import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.projection.GameProjection;
import xyz.zlatanov.frakkintoasters.projection.LiveUpdateProjection;

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

    /**
     * Client to server.
     */
    public sealed interface ClientMessage {
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
            java.util.Map.entry(StatusMessage.class, "status"),
            java.util.Map.entry(HistoryMessage.class, "history"),
            java.util.Map.entry(SubmitMessage.class, "submit"),
            java.util.Map.entry(ChooseMessage.class, "choose"),
            java.util.Map.entry(UndoMessage.class, "undo"),
            java.util.Map.entry(InjectMessage.class, "inject"));

}
