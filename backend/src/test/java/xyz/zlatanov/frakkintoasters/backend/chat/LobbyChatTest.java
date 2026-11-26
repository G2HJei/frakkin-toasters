package xyz.zlatanov.frakkintoasters.backend.chat;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import xyz.zlatanov.frakkintoasters.backend.BackendTest;
import xyz.zlatanov.frakkintoasters.backend.chat.LobbyChat.LobbyLine;
import xyz.zlatanov.frakkintoasters.backend.chat.LobbyChat.Message;
import xyz.zlatanov.frakkintoasters.backend.user.AppUserAuthentication.AppUserPrincipal;
import xyz.zlatanov.frakkintoasters.backend.user.Users;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class LobbyChatTest extends BackendTest {

    @Autowired
    Users users;

    @Test
    void keepsTheLastMessagesInMemoryOldestFirst() {
        val clock = new TestClock();
        val chat = new LobbyChat(users, clock);
        val adama = createUser("Adama");

        val first = chat.post(new AppUserPrincipal(adama), new Message("  Action stations  "));
        assertEquals(new LobbyLine(first.id(), adama.toString(), "Adama", "Action stations", clock.instant().toString()), first);
        for (int i = 0; i < LobbyChat.HISTORY; i++) {
            clock.advance(Duration.ofSeconds(3));
            chat.post(new AppUserPrincipal(adama), new Message("Message " + i));
        }

        val lines = chat.lines();
        assertEquals(LobbyChat.HISTORY, lines.size());
        assertEquals(IntStream.range(0, LobbyChat.HISTORY).mapToObj(i -> "Message " + i).toList(),
                lines.stream().map(LobbyLine::text).toList());
        assertTrue(lines.getFirst().id() < lines.getLast().id());
    }

    @Test
    void refusesAnEmptyOrLongMessageAndOneTooMany() {
        val clock = new TestClock();
        val chat = new LobbyChat(users, clock);
        val roslin = new AppUserPrincipal(createUser("Roslin"));

        assertEquals(HttpStatus.BAD_REQUEST, refusal(() -> chat.post(roslin, new Message("   "))));
        assertEquals(HttpStatus.BAD_REQUEST, refusal(() -> chat.post(roslin, new Message("x".repeat(Chat.MAX_LENGTH + 1)))));
        chat.post(roslin, new Message("x".repeat(Chat.MAX_LENGTH)));
        for (int i = 1; i < Chat.BURST; i++) {
            chat.post(roslin, new Message("So say we all"));
        }
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, refusal(() -> chat.post(roslin, new Message("One more"))));
        // another user still writes, and so does she once the window has passed
        chat.post(new AppUserPrincipal(createUser("Tigh")), new Message("Frak"));
        clock.advance(Chat.WINDOW);
        chat.post(roslin, new Message("One more"));
        assertEquals(Chat.BURST + 2, chat.lines().size());
    }

    private static HttpStatus refusal(Runnable post) {
        return HttpStatus.valueOf(assertThrows(ResponseStatusException.class, post::run).getStatusCode().value());
    }

    private static final class TestClock extends Clock {

        private Instant now = Instant.parse("2026-11-26T12:00:00Z");

        void advance(Duration duration) {
            now = now.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }
}
