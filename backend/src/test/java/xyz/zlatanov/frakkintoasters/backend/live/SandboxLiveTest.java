package xyz.zlatanov.frakkintoasters.backend.live;

import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.JsonNodeFactory;
import xyz.zlatanov.frakkintoasters.backend.TestcontainersConfiguration;
import xyz.zlatanov.frakkintoasters.backend.game.GameBundles;
import xyz.zlatanov.frakkintoasters.backend.game.GameSessions;
import xyz.zlatanov.frakkintoasters.backend.journal.JournalCodec;
import xyz.zlatanov.frakkintoasters.backend.lobby.Participants;
import xyz.zlatanov.frakkintoasters.engine.GameEngine;
import xyz.zlatanov.frakkintoasters.engine.GiveSkillCardEvent;
import xyz.zlatanov.frakkintoasters.engine.ScriptEvent;
import xyz.zlatanov.frakkintoasters.engine.ScriptedTurns;
import xyz.zlatanov.frakkintoasters.engine.TestEngines;
import xyz.zlatanov.frakkintoasters.event.deck.CreateDestinyDeckEvent;
import xyz.zlatanov.frakkintoasters.event.endgame.CylonsWinEvent;
import xyz.zlatanov.frakkintoasters.event.interrupt.PlayInterruptCardEvent;
import xyz.zlatanov.frakkintoasters.event.player.SelectCharacterEvent;
import xyz.zlatanov.frakkintoasters.event.sandbox.ChangeResourceEvent;
import xyz.zlatanov.frakkintoasters.event.sandbox.PutCrisisCardOnTopEvent;
import xyz.zlatanov.frakkintoasters.event.sandbox.SandboxEvent;
import xyz.zlatanov.frakkintoasters.event.skillcheck.result.KeepTabsOnVisitorFailEvent;
import xyz.zlatanov.frakkintoasters.state.board.Resource;
import xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.STRATEGIC_PLANNING;

// A sandbox over the web socket: one user plays every seat, undoes, exports, imports and sets up scenarios.
@SpringBootTest(webEnvironment = RANDOM_PORT, properties = "frakkin.take-only-answers=false")
@ActiveProfiles("dev")
@Import(TestcontainersConfiguration.class)
class SandboxLiveTest {

    private static final JsonNodeFactory NODES = JsonNodeFactory.instance;

    @LocalServerPort
    int port;

    @Autowired
    JournalCodec codec;
    @Autowired
    GameSessions sessions;
    @Autowired
    GameBundles  bundles;
    @Autowired
    Participants participants;

    private final List<TestClient> clients = new ArrayList<>();

    @AfterEach
    void close() {
        clients.forEach(TestClient::close);
    }

    @Test
    void oneClientPlaysASeededSandboxThroughTheCharactersAndATurnAnsweringEverySeat() {
        val client = client();
        startSandbox(client, 42);
        // the same game in memory, for the inputs, which depend on the first player the seed picks
        val inMemory = GameEngine.start(3, 42L);
        val inputs = ScriptedTurns.inputs();

        for (val next : inputs.subList(0, 3)) {
            play(client, inMemory, next.apply(inMemory.view()));
        }
        // the turn's Crisis Card is stacked, so the inputs that follow fit it
        inject(client, inMemory, new PutCrisisCardOnTopEvent(CrisisCard.RIOTS_NO_JUMP));
        // through turn 1's Crisis Card, which Kara "Starbuck" Thrace lets resolve before the Admiral chooses
        for (val next : inputs.subList(3, 12)) {
            play(client, inMemory, next.apply(inMemory.view()));
        }

        assertEquals(2, client.game().get("turn").get("number").intValue());
        assertEquals(List.of(CrisisCard.RIOTS_NO_JUMP), inMemory.view().decks().<CrisisCard>deck(
                xyz.zlatanov.frakkintoasters.state.deck.DeckId.CRISIS).discardedCards());
        assertTrue(client.game().get("decks").get(0).get("drawPile").isArray()); // the sandbox sees the draw piles
    }

    @Test
    void undoThenTheSameInputGivesTheSameGame() {
        val client = client();
        startSandbox(client, 7);
        val inMemory = GameEngine.start(3, 7L);
        val inputs = ScriptedTurns.inputs();
        play(client, inMemory, inputs.get(0).apply(inMemory.view()));
        val second = inputs.get(1).apply(inMemory.view());
        play(client, inMemory, second);
        val after = client.game();

        client.send(message("undo", client.token()));
        client.next("game");
        assertEquals("log", client.next("log").get("type").stringValue()); // the client replaces its log
        assertNotEquals(after, client.game());
        send(client, second);
        client.nextGame();

        assertEquals(after, client.game());
    }

    @Test
    void anExportedSandboxImportsAsTheSameGame() {
        val client = client();
        val game = startSandbox(client, 11);
        val inMemory = GameEngine.start(3, 11L);
        for (val next : ScriptedTurns.inputs().subList(0, 4)) {
            play(client, inMemory, next.apply(inMemory.view()));
        }
        val original = client.game();

        val file = (tools.jackson.databind.node.ObjectNode) client.get("/api/games/" + game + "/journal");
        assertEquals(JournalCodec.FORMAT_VERSION, file.get("formatVersion").intValue());
        assertEquals(11, file.get("seed").longValue());
        val imported = UUID.fromString(client.post("/api/sandbox/import", JournalCodec.stringify(file)).get("id").stringValue());
        client.close();
        client.connect(imported);
        client.next("game");
        client.next("log");

        assertEquals(original, client.game());
        // another version's journal is refused, with a message
        file.put("formatVersion", JournalCodec.FORMAT_VERSION + 1);
        val refused = assertThrows(AssertionError.class, () -> client.post("/api/sandbox/import", JournalCodec.stringify(file)));
        assertTrue(refused.getMessage().startsWith("400"), refused.getMessage());
    }

    @Test
    void aSetupRunsDuringASecretInterruptWindow() {
        val client = client();
        val userId = client.login("Sandboxer " + UUID.randomUUID().toString().substring(0, 6));
        val planning = new SkillCard(3, STRATEGIC_PLANNING);
        val game = bundles.createSandbox("window", userId, JournalCodec.FORMAT_VERSION);
        participants.add(game, userId, null);
        sessions.start(game, TestEngines.start(3, 5L, new ScriptEvent(all(
                all(new SelectCharacterEvent(1, WILLIAM_ADAMA), new SelectCharacterEvent(2, LAURA_ROSLIN),
                        new SelectCharacterEvent(3, KARA_STARBUCK_THRACE), new CreateDestinyDeckEvent()),
                single(new GiveSkillCardEvent(2, planning)),
                single(new KeepTabsOnVisitorFailEvent()),
                single(new CylonsWinEvent())))));
        client.connect(game);
        client.next("game");
        client.next("log");
        val input = client.game().get("input");
        assertEquals(List.of("Decide", "PlayInterruptCardEvent", "2"), List.of(input.get("kind").stringValue(),
                input.get("action").stringValue(), input.get("player").asString())); // the sandbox sees it in full
        val food = client.game().get("galactica").get("food").intValue();

        client.send(withEvent("inject", client.token(), codec.writeEvent(new ChangeResourceEvent(Resource.FOOD, -1))));
        client.nextGame();

        assertEquals(food - 1, client.game().get("galactica").get("food").intValue());
        assertEquals(input, client.game().get("input")); // the window still waits
        client.submit(codec.writeEvent(PlayInterruptCardEvent.pass(2))); // the sandbox answers for player 2
        assertEquals("CYLONS", client.nextGame().get("game").get("outcome").get("winner").stringValue());
    }

    @Test
    void undoInjectAndExportAreRefusedInAStandardGame() {
        val host = client();
        val suffix = UUID.randomUUID().toString().substring(0, 6);
        host.login("Host " + suffix);
        val others = List.of(client(), client());
        others.get(0).login("Other " + suffix);
        others.get(1).login("Third " + suffix);
        val game = UUID.fromString(host.post("/api/games", "{\"name\":\"Standard " + suffix + "\"}").get("id").stringValue());
        others.forEach(other -> other.post("/api/games/" + game + "/join", "{\"as\":\"PLAYER\"}"));
        sessions.start(game, ScriptedTurns.start());
        host.connect(game);
        host.next("game");
        host.next("log");

        host.send(message("undo", host.token()));
        assertEquals("INVALID", host.next("rejected").get("reason").stringValue());
        host.send(withEvent("inject", host.token(), codec.writeEvent(new ChangeResourceEvent(Resource.FOOD, -1))));
        assertEquals("INVALID", host.next("rejected").get("reason").stringValue());
        val export = assertThrows(AssertionError.class, () -> host.get("/api/games/" + game + "/journal"));
        assertTrue(export.getMessage().startsWith("403"), export.getMessage());
        // nobody else joins a sandbox
        val sandbox = startSandbox(others.get(0), 3);
        val join = assertThrows(AssertionError.class, () -> host.post("/api/games/" + sandbox + "/join", "{\"as\":\"OBSERVER\"}"));
        assertTrue(join.getMessage().startsWith("403"), join.getMessage());
    }

    @Test
    void aReconnectGetsTheGameAndThenTheLog() {
        val client = client();
        val game = startSandbox(client, 9);
        val token = client.token();

        client.close();
        client.connect(game);

        assertEquals(token, client.next("game").get("token").stringValue());
        assertFalse(client.next("log").get("log").get("entries").isEmpty());
    }

    /* helpers */

    private TestClient client() {
        val client = new TestClient(port);
        clients.add(client);
        return client;
    }

    private UUID startSandbox(TestClient client, long seed) {
        if (!client.loggedIn()) {
            client.login("Sandboxer " + UUID.randomUUID().toString().substring(0, 6));
        }
        val game = UUID.fromString(client.post("/api/sandbox", "{\"playerCount\":3,\"seed\":" + seed + "}")
                .get("id").stringValue());
        client.connect(game);
        client.next("game");
        client.next("log");
        return game;
    }

    // Plays the input in memory and over the socket, as the sandbox, which answers for every seat.
    private void play(TestClient client, GameEngine inMemory, ScriptedTurns.Input input) {
        val options = client.game().get("input").get("options");
        input.applyTo(inMemory);
        send(client, input, options);
        client.nextGame();
    }

    private void inject(TestClient client, GameEngine inMemory, SandboxEvent setup) {
        inMemory.inject(setup);
        client.send(withEvent("inject", client.token(), codec.writeEvent(setup)));
        client.nextGame();
    }

    private void send(TestClient client, ScriptedTurns.Input input) {
        send(client, input, client.game().get("input").get("options"));
    }

    private void send(TestClient client, ScriptedTurns.Input input, JsonNode options) {
        if (input.submit() != null) {
            client.submit(codec.writeEvent(input.submit()));
            return;
        }
        val option = codec.values().write(input.choose(), xyz.zlatanov.frakkintoasters.event.Followup.class);
        for (int i = 0; i < options.size(); i++) {
            if (options.get(i).equals(option)) {
                client.choose(i);
                return;
            }
        }
        fail("Not an option: " + option + " of " + options);
    }

    private static JsonNode message(String type, String token) {
        val message = NODES.objectNode();
        message.put("type", type);
        message.put("token", token);
        return message;
    }

    private static JsonNode withEvent(String type, String token, JsonNode event) {
        val message = NODES.objectNode();
        message.put("type", type);
        message.put("token", token);
        message.set("event", event);
        return message;
    }
}
