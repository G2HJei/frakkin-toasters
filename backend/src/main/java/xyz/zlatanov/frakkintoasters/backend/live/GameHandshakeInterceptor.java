package xyz.zlatanov.frakkintoasters.backend.live;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import xyz.zlatanov.frakkintoasters.backend.game.GameBundle.GameMode;
import xyz.zlatanov.frakkintoasters.backend.game.GameBundle.GameStatus;
import xyz.zlatanov.frakkintoasters.backend.game.GameBundles;
import xyz.zlatanov.frakkintoasters.backend.lobby.Participants;
import xyz.zlatanov.frakkintoasters.backend.lobby.Seats;
import xyz.zlatanov.frakkintoasters.backend.user.AppUserAuthentication;
import xyz.zlatanov.frakkintoasters.projection.Viewer;

import java.util.Map;
import java.util.UUID;

/**
 * The handshake of {@code /ws/games/{gameBundleId}} needs a logged in user who is a participant of the game, or any user
 * but a banned one for a finished game, which he watches: its URL is a read-only link. That gives the connection its
 * user, its game and its viewer: player n, an observer, or the sandbox.
 */
@Component
@RequiredArgsConstructor
public class GameHandshakeInterceptor implements HandshakeInterceptor {

    static final String GAME   = "gameBundleId";
    static final String USER   = "userId";
    static final String VIEWER = "viewer";

    private final Participants participants;
    private final GameBundles  bundles;
    private final Seats        seats;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler handler,
                                   Map<String, Object> attributes) {
        if (!(request.getPrincipal() instanceof AppUserAuthentication authentication)) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }
        val path = request.getURI().getPath();
        final UUID gameBundleId;
        try {
            gameBundleId = UUID.fromString(path.substring(path.lastIndexOf('/') + 1));
        } catch (IllegalArgumentException e) {
            response.setStatusCode(HttpStatus.NOT_FOUND);
            return false;
        }
        val userId = authentication.getPrincipal().id();
        val participant = participants.find(gameBundleId, userId);
        val bundle = bundles.find(gameBundleId);
        val watches = bundle.isPresent() && participant.isEmpty() && bundle.get().mode() == GameMode.STANDARD
                && bundle.get().status() == GameStatus.FINISHED && !seats.banned(gameBundleId, userId);
        if (bundle.isEmpty() || participant.isEmpty() && !watches) {
            response.setStatusCode(HttpStatus.FORBIDDEN);
            return false;
        }
        attributes.put(GAME, gameBundleId);
        attributes.put(USER, userId);
        attributes.put(VIEWER, participant.map(seated -> viewer(bundle.get().mode(), seated)).orElse(Viewer.observer()));
        return true;
    }

    static Viewer viewer(GameMode mode, Participants.Participant participant) {
        if (mode == GameMode.SANDBOX) {
            return Viewer.sandbox();
        }
        return participant.isPlayer() ? Viewer.player(participant.playerNumber()) : Viewer.observer();
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler handler,
                               Exception exception) {
    }
}
