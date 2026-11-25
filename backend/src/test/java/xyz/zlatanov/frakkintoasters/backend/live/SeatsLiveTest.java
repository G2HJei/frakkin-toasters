package xyz.zlatanov.frakkintoasters.backend.live;

import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import xyz.zlatanov.frakkintoasters.backend.TestcontainersConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

// A seat changing hands over real sockets: the connections of both users reconnect with their new viewer.
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("dev")
@Import(TestcontainersConfiguration.class)
class SeatsLiveTest {

    @LocalServerPort
    int port;

    private final List<TestClient> clients = new ArrayList<>();

    @AfterEach
    void close() {
        clients.forEach(TestClient::close);
    }

    @Test
    void aSeatLeftAndTakenReconnectsBothUsersWithTheirNewViewer() {
        val game = startedGame();
        val starbuck = clients.get(2);
        val dualla = clients.get(3);

        starbuck.post("/api/games/" + game + "/leave", "{}");

        assertEquals(ConnectionPool.SEAT_CHANGED.getCode(), starbuck.awaitClose());
        starbuck.connect(game);
        assertEquals("Observer", starbuck.next("game").get("game").get("viewer").get("kind").stringValue());

        dualla.post("/api/games/" + game + "/seat", "{\"playerNumber\":3}");

        assertEquals(ConnectionPool.SEAT_CHANGED.getCode(), dualla.awaitClose());
        dualla.connect(game);
        val viewer = dualla.next("game").get("game").get("viewer");
        assertEquals("PlayerViewer", viewer.get("kind").stringValue());
        assertEquals(3, viewer.get("playerNumber").intValue());
    }

    @Test
    void anyoneWatchesAFinishedGameButNotARunningOne() {
        val game = startedGame();
        val stranger = client("Ellen");
        assertThrows(Exception.class, () -> stranger.connect(game));

        clients.getFirst().post("/api/games/" + game + "/votes", "{\"kind\":\"ABANDON\",\"playerNumber\":0}");

        stranger.connect(game);
        val watched = stranger.next("game").get("game");
        assertEquals("Observer", watched.get("viewer").get("kind").stringValue());
        assertTrue(watched.get("input").isNull());
        stranger.next("log");
    }

    // Three players and an observer, started through the lobby, each with a socket.
    private UUID startedGame() {
        for (val name : List.of("Adama", "Roslin", "Starbuck", "Dualla")) {
            client(name);
        }
        val room = clients.get(0).post("/api/games", "{\"name\":\"Seats\"}");
        val game = UUID.fromString(room.get("id").stringValue());
        clients.get(1).post("/api/games/" + game + "/join", "{\"as\":\"PLAYER\"}");
        clients.get(2).post("/api/games/" + game + "/join", "{\"as\":\"PLAYER\"}");
        clients.get(3).post("/api/games/" + game + "/join", "{\"as\":\"OBSERVER\"}");
        clients.get(0).post("/api/games/" + game + "/start", "{}");
        for (val client : List.copyOf(clients)) {
            client.connect(game);
            client.next("game");
            client.next("log");
        }
        return game;
    }

    private TestClient client(String name) {
        val client = new TestClient(port);
        client.login(name + " " + UUID.randomUUID().toString().substring(0, 6));
        clients.add(client);
        return client;
    }
}
