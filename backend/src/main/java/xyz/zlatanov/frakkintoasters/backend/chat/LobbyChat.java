package xyz.zlatanov.frakkintoasters.backend.chat;

import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import xyz.zlatanov.frakkintoasters.backend.user.AppUser;
import xyz.zlatanov.frakkintoasters.backend.user.AppUserAuthentication.AppUserPrincipal;
import xyz.zlatanov.frakkintoasters.backend.user.Users;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * The chat of the play page, for everyone logged in there. It lives in memory while the app runs: the last
 * {@value #HISTORY} messages, gone with a restart. No socket: the page asks for them every few seconds. Messages are
 * short, and a user writes only so many in a row, as at a table ({@link Chat}).
 */
@RestController
@RequestMapping("/api/lobby/chat")
public class LobbyChat {

    public static final int HISTORY = 64;

    private final Users                     users;
    private final Clock                     clock;
    // the last messages, oldest first, and the times of each user's last messages, for the rate limit
    private final Deque<LobbyLine>          lines  = new ArrayDeque<>();
    private final Map<UUID, Deque<Instant>> recent = new HashMap<>();
    private       long                      lastId;

    /**
     * @param at when it was written, as an ISO instant
     */
    public record LobbyLine(long id, String userId, String name, String text, String at) {
    }

    public record Message(String text) {
    }

    @Autowired
    public LobbyChat(Users users) {
        this(users, Clock.systemUTC());
    }

    LobbyChat(Users users, Clock clock) {
        this.users = users;
        this.clock = clock;
    }

    /**
     * @return the last messages, oldest first
     */
    @GetMapping
    public synchronized List<LobbyLine> lines() {
        return List.copyOf(lines);
    }

    /**
     * Keeps the user's message under his name now, dropping the oldest past {@value #HISTORY}.
     */
    @PostMapping
    public synchronized LobbyLine post(@AuthenticationPrincipal AppUserPrincipal principal, @RequestBody Message message) {
        val name = users.find(principal.id()).map(AppUser::name)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        val text = message.text() == null ? "" : message.text().strip();
        if (text.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The message is empty");
        }
        if (text.length() > Chat.MAX_LENGTH) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A message has at most " + Chat.MAX_LENGTH + " characters");
        }
        val now = clock.instant();
        throttle(principal.id(), now);
        val line = new LobbyLine(++lastId, principal.id().toString(), name, text, now.toString());
        lines.addLast(line);
        while (lines.size() > HISTORY) {
            lines.pollFirst();
        }
        return line;
    }

    // At most Chat.BURST messages in any Chat.WINDOW. A user who wrote nothing in the window is forgotten.
    private void throttle(UUID userId, Instant now) {
        recent.values().forEach(times -> forgetBefore(times, now));
        recent.values().removeIf(Deque::isEmpty);
        val times = recent.computeIfAbsent(userId, ignored -> new ArrayDeque<>());
        if (times.size() >= Chat.BURST) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "Slow down: at most " + Chat.BURST + " messages in " + Chat.WINDOW.toSeconds() + " seconds");
        }
        times.addLast(now);
    }

    private static void forgetBefore(Deque<Instant> times, Instant now) {
        while (!times.isEmpty() && !times.peekFirst().isAfter(now.minus(Chat.WINDOW))) {
            times.pollFirst();
        }
    }
}
