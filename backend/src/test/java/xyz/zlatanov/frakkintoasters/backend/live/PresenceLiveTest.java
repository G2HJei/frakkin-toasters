package xyz.zlatanov.frakkintoasters.backend.live;

import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.socket.CloseStatus;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.JsonNodeFactory;
import xyz.zlatanov.frakkintoasters.backend.TestcontainersConfiguration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

// The silence is short, so the sweep comes within the test.
@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {"frakkin.socket-silence=3s", "frakkin.socket-sweep=250ms"})
@ActiveProfiles("dev")
@Import(TestcontainersConfiguration.class)
class PresenceLiveTest {

    @LocalServerPort
    int port;

    private final List<TestClient> clients = new ArrayList<>();
    private final List<UUID>       users   = new ArrayList<>();

    @AfterEach
    void close() {
        clients.forEach(TestClient::close);
    }

    @Test
    void aLobbyShowsWhoPollsItsRoomAndARunningGameWhoIsConnected() {
        val game = lobby();
        clients.get(1).get("/api/games/" + game); // the others' room pages poll too

        assertEquals(Set.of(users.get(0), users.get(1)), online(clients.get(0).get("/api/games/" + game)));

        clients.get(2).get("/api/games/" + game);
        clients.get(0).post("/api/games/" + game + "/start", "{}");
        clients.get(0).connect(game);
        clients.get(0).next("game");
        clients.get(1).connect(game);
        clients.get(1).next("game");

        assertEquals(Set.of(users.get(0), users.get(1)), online(clients.get(2).get("/api/games/" + game)));
        clients.get(1).close();
        awaitOnline(game, Set.of(users.get(0)));
    }

    @Test
    void aConnectionThatStopsPingingIsClosedAndOnePingingStays() throws Exception {
        val game = lobby();
        clients.get(0).post("/api/games/" + game + "/start", "{}");
        val pinging = clients.get(0);
        val silent = clients.get(1);
        for (val client : List.of(pinging, silent)) {
            client.connect(game);
            client.next("game");
            client.next("log");
        }

        for (int i = 0; i < 8; i++) {
            pinging.send(JsonNodeFactory.instance.objectNode().put("type", "ping"));
            assertEquals("pong", pinging.next("pong").get("type").stringValue());
            Thread.sleep(500);
        }

        assertEquals(CloseStatus.SESSION_NOT_RELIABLE.getCode(), silent.awaitClose());
        awaitOnline(game, Set.of(users.get(0)));
        pinging.send(JsonNodeFactory.instance.objectNode().put("type", "ping"));
        pinging.next("pong");
    }

    private UUID lobby() {
        for (val name : List.of("Adama", "Roslin", "Starbuck")) {
            val client = new TestClient(port);
            users.add(client.login(name + " " + UUID.randomUUID().toString().substring(0, 6)));
            clients.add(client);
        }
        val game = UUID.fromString(clients.get(0).post("/api/games", "{\"name\":\"Presence\"}").get("id").stringValue());
        clients.get(1).post("/api/games/" + game + "/join", "{\"as\":\"PLAYER\"}");
        clients.get(2).post("/api/games/" + game + "/join", "{\"as\":\"PLAYER\"}");
        return game;
    }

    private void awaitOnline(UUID game, Set<UUID> expected) {
        val until = System.currentTimeMillis() + 10_000;
        Set<UUID> online;
        do {
            online = online(clients.get(2).get("/api/games/" + game));
            if (online.equals(expected)) {
                return;
            }
        } while (System.currentTimeMillis() < until);
        assertEquals(expected, online);
    }

    private static Set<UUID> online(JsonNode room) {
        val online = new HashSet<UUID>();
        room.get("online").forEach(id -> online.add(UUID.fromString(id.stringValue())));
        return online;
    }
}
