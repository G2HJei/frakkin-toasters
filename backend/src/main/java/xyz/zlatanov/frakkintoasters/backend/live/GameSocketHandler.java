package xyz.zlatanov.frakkintoasters.backend.live;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import tools.jackson.databind.JsonNode;
import xyz.zlatanov.frakkintoasters.backend.game.CommandResult;
import xyz.zlatanov.frakkintoasters.backend.game.CommandResult.Rejected;
import xyz.zlatanov.frakkintoasters.backend.game.GameNotAvailableException;
import xyz.zlatanov.frakkintoasters.backend.game.GameSessions;
import xyz.zlatanov.frakkintoasters.backend.journal.CodecException;
import xyz.zlatanov.frakkintoasters.backend.journal.JournalCodec;
import xyz.zlatanov.frakkintoasters.projection.Viewer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@code /ws/games/{gameBundleId}}. A new connection gets the game and the whole log. The client sends JSON with a
 * {@code type}:
 * <ul>
 *     <li>{@code status}: the game again</li>
 *     <li>{@code history}: the game and the whole log again</li>
 *     <li>{@code submit}: the token and an {@code event}, which answers the pending decision</li>
 *     <li>{@code choose}: the token and the index of an {@code option} of the pending choice</li>
 *     <li>{@code undo}: the token; a sandbox goes back to the pause before its last input</li>
 *     <li>{@code inject}: the token and a setup {@code event}, which a sandbox runs ahead of the pending input</li>
 * </ul>
 * After a command, every connection with something new gets the update and then the game. A rejection goes only to the
 * sender, with the current game when the command was stale.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GameSocketHandler extends TextWebSocketHandler {

    private final GameSessions                         sessions;
    private final ConnectionPool                       pool;
    private final Messages                             messages;
    private final Map<String, LiveConnection>          connections = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        session.setTextMessageSizeLimit(512 * 1024); // an injected event, at most
        val attributes = session.getAttributes();
        val connection = new LiveConnection(session, (UUID) attributes.get(GameHandshakeInterceptor.GAME),
                (UUID) attributes.get(GameHandshakeInterceptor.USER), (Viewer) attributes.get(GameHandshakeInterceptor.VIEWER));
        connections.put(session.getId(), connection);
        try {
            sessions.connect(connection.gameBundleId(), connection.viewer(), frames -> {
                pool.add(connection);
                messages.of(frames).forEach(connection::send);
            });
        } catch (GameNotAvailableException e) {
            connection.send(messages.rejected("UNAVAILABLE", e.getMessage()));
            connection.close(CloseStatus.POLICY_VIOLATION.withReason("The game can't be opened"));
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage text) {
        val connection = connections.get(session.getId());
        if (connection == null) {
            return;
        }
        try {
            handle(connection, JournalCodec.parse(text.getPayload()));
        } catch (CodecException e) {
            connection.send(messages.rejected("INVALID", e.getMessage()));
        } catch (GameNotAvailableException e) {
            connection.send(messages.rejected("UNAVAILABLE", e.getMessage()));
        } catch (RuntimeException e) {
            log.error("Message on {} failed", session.getId(), e);
            connection.send(messages.rejected("FAILURE", "The server failed"));
        }
    }

    private void handle(LiveConnection connection, JsonNode message) {
        val game = connection.gameBundleId();
        val viewer = connection.viewer();
        val type = message.path("type").asString("");
        switch (type) {
            case "status" -> connection.send(messages.game(sessions.status(game, viewer)));
            case "history" -> messages.of(sessions.history(game, viewer)).forEach(connection::send);
            case "submit" -> reply(connection, sessions.submit(game, viewer, token(message), message.get("event")));
            case "choose" -> {
                val option = message.get("option");
                if (option == null || !option.isInt()) {
                    throw new CodecException("A choice needs the index of an option");
                }
                reply(connection, sessions.choose(game, viewer, token(message), option.intValue()));
            }
            case "undo" -> reply(connection, sessions.undo(game, viewer, token(message)));
            case "inject" -> reply(connection, sessions.inject(game, viewer, token(message), message.get("event")));
            default -> throw new CodecException("Unknown message type '" + type + "'");
        }
    }

    // An accepted command reached everyone through the pool already.
    private void reply(LiveConnection connection, CommandResult result) {
        if (result instanceof Rejected rejected) {
            connection.send(messages.rejected(rejected));
            if (rejected.game() != null) {
                connection.send(messages.game(rejected.game()));
            }
        }
    }

    private static String token(JsonNode message) {
        val token = message.get("token");
        return token != null && token.isString() ? token.stringValue() : null;
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        val connection = connections.remove(session.getId());
        if (connection != null) {
            pool.remove(connection);
            connection.close(status);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.debug("Transport error on {}", session.getId(), exception);
    }
}
