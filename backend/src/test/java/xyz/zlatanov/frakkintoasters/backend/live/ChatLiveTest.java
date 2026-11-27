package xyz.zlatanov.frakkintoasters.backend.live;

import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import tools.jackson.databind.JsonNode;
import xyz.zlatanov.frakkintoasters.backend.TestcontainersConfiguration;
import xyz.zlatanov.frakkintoasters.backend.chat.Chat;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("dev")
@Import(TestcontainersConfiguration.class)
class ChatLiveTest {

    @LocalServerPort
    int port;

    private final List<TestClient> clients = new ArrayList<>();

    @AfterEach
    void close() {
        clients.forEach(TestClient::close);
    }

    @Test
    void aMessageReachesEveryoneAtTheTableWithItsWritersSeat() {
        startedGame();
        val adama = clients.get(0);
        val dualla = clients.get(3);

        adama.say("  Roslin is a Cylon.  ");

        for (val client : clients) {
            val line = client.nextChat("chat").get("message");
            assertEquals("Roslin is a Cylon.", line.get("text").stringValue());
            assertEquals(1, line.get("playerNumber").intValue());
        }

        dualla.say("He is bluffing");

        for (val client : clients) {
            val line = client.nextChat("chat").get("message");
            assertEquals("He is bluffing", line.get("text").stringValue());
            assertTrue(line.get("playerNumber").isNull(), "an observer holds no seat");
        }
    }

    @Test
    void aMessageIsShortAndAUserWritesOnlySoManyInARow() {
        startedGame();
        val adama = clients.get(0);

        adama.say("x".repeat(Chat.MAX_LENGTH + 1));
        assertTrue(adama.nextChat("chatRejected").get("message").stringValue().contains("at most"));
        adama.say("   ");
        assertTrue(adama.nextChat("chatRejected").get("message").stringValue().contains("empty"));
        for (int i = 0; i < 5; i++) {
            adama.say("Action stations " + i);
            adama.nextChat("chat");
        }
        adama.say("one too many");
        assertTrue(adama.nextChat("chatRejected").get("message").stringValue().contains("Slow down"));
        for (int i = 0; i < 5; i++) {
            clients.get(1).nextChat("chat");
        }
        clients.get(1).assertNoChat();
    }

    @Test
    void aNewConnectionGetsTheLastMessages() {
        val game = startedGame();
        // two sockets race to the server: the second is sent once the first is stored, which it is before it reaches anyone
        clients.get(0).say("So say we all");
        clients.forEach(client -> client.nextChat("chat"));
        clients.get(3).say("Popcorn");
        clients.forEach(client -> client.nextChat("chat"));

        clients.get(1).connect(game);

        assertEquals(List.of("So say we all", "Popcorn"), texts(clients.get(1).nextChat("chatLog")));
    }

    private static List<String> texts(JsonNode chatLog) {
        val texts = new ArrayList<String>();
        chatLog.get("messages").forEach(line -> texts.add(line.get("text").stringValue()));
        return texts;
    }

    private UUID startedGame() {
        for (val name : List.of("Adama", "Roslin", "Starbuck", "Dualla")) {
            val client = new TestClient(port);
            client.login(name + " " + UUID.randomUUID().toString().substring(0, 6));
            clients.add(client);
        }
        val game = UUID.fromString(clients.get(0).post("/api/games", "{\"name\":\"Chat\"}").get("id").stringValue());
        clients.get(1).post("/api/games/" + game + "/join", "{\"as\":\"PLAYER\"}");
        clients.get(2).post("/api/games/" + game + "/join", "{\"as\":\"PLAYER\"}");
        clients.get(3).post("/api/games/" + game + "/join", "{\"as\":\"OBSERVER\"}");
        clients.get(0).post("/api/games/" + game + "/start", "{}");
        for (val client : clients) {
            client.connect(game);
            client.next("game");
            client.next("log");
            assertEquals(0, client.nextChat("chatLog").get("messages").size());
        }
        return game;
    }
}
