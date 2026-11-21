package xyz.zlatanov.frakkintoasters.backend.live;

import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.postgresql.PostgreSQLContainer;
import tools.jackson.databind.JsonNode;
import xyz.zlatanov.frakkintoasters.backend.FrakkinToastersApplication;
import xyz.zlatanov.frakkintoasters.backend.TestcontainersConfiguration;
import xyz.zlatanov.frakkintoasters.backend.game.GameSessions;
import xyz.zlatanov.frakkintoasters.backend.journal.JournalCodec;
import xyz.zlatanov.frakkintoasters.engine.GameEngine;
import xyz.zlatanov.frakkintoasters.engine.GiveSkillCardEvent;
import xyz.zlatanov.frakkintoasters.engine.ScriptEvent;
import xyz.zlatanov.frakkintoasters.engine.ScriptedTurns;
import xyz.zlatanov.frakkintoasters.engine.TestEngines;
import xyz.zlatanov.frakkintoasters.event.deck.CreateDestinyDeckEvent;
import xyz.zlatanov.frakkintoasters.event.endgame.CylonsWinEvent;
import xyz.zlatanov.frakkintoasters.event.interrupt.PlayInterruptCardEvent;
import xyz.zlatanov.frakkintoasters.event.player.SelectCharacterEvent;
import xyz.zlatanov.frakkintoasters.event.skillcheck.result.KeepTabsOnVisitorFailEvent;
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

// Real web socket clients against the running application: three players and an observer.
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("dev")
@Import(TestcontainersConfiguration.class)
class LiveGameTest {

    @LocalServerPort
    int port;

    @Autowired
    GameSessions        sessions;
    @Autowired
    JournalCodec        codec;
    @Autowired
    PostgreSQLContainer postgres;
    @Autowired
    JdbcClient          jdbc;

    private final List<TestClient> clients = new ArrayList<>();

    @AfterEach
    void close() {
        clients.forEach(TestClient::close);
    }

    @Test
    void threePlayersAndAnObserverPlayTheScriptedTurns() {
        scriptedGame();
        val tokens = new ArrayList<String>();
        tokens.add(clients.getFirst().token());
        val inMemory = ScriptedTurns.start();

        for (val next : ScriptedTurns.inputs()) {
            val input = next.apply(inMemory.view());
            input.applyTo(inMemory);
            val sender = clients.get(input.player() - 1);
            sender.awaitToken(tokens.getLast()); // the state the input answers
            tokens.add(send(sender, input));
        }

        // everyone ends at the last state, and got the states in the order of the commands
        for (val client : clients) {
            client.awaitToken(tokens.getLast());
            val received = client.received().stream()
                    .filter(message -> message.get("type").stringValue().equals("game"))
                    .map(message -> message.get("token").stringValue())
                    .toList();
            assertEquals(received, tokens.stream().filter(received::contains).toList());
        }
        assertEquals(5, clients.getLast().game().get("turn").get("number").intValue());
        // no frame of a client holds a card that only another player knows
        for (int viewer = 1; viewer <= 4; viewer++) {
            assertNoCardsOfOthers(clients.get(viewer - 1), viewer);
        }
    }

    @Test
    void aSubmitFromTheWrongPlayerOrWithAnOldTokenIsRejectedAndNobodyElseHearsOfIt() {
        scriptedGame();
        val inMemory = ScriptedTurns.start();
        val first = ScriptedTurns.inputs().getFirst().apply(inMemory.view());
        val other = clients.get(first.player() % 3);

        other.submit(codec.writeEvent(first.submit()));

        assertEquals("INVALID", other.next("rejected").get("reason").stringValue());
        clients.stream().filter(client -> client != other).forEach(TestClient::assertNothingCame);

        val oldToken = clients.get(first.player() - 1).token();
        first.applyTo(inMemory);
        send(clients.get(first.player() - 1), first);
        val firstToken = clients.get(first.player() - 1).token();
        clients.forEach(client -> client.awaitToken(firstToken));
        val second = ScriptedTurns.inputs().get(1).apply(inMemory.view());
        val sender = clients.get(second.player() - 1);

        sender.submit(oldToken, codec.writeEvent(second.submit()));

        assertEquals("STALE", sender.next("rejected").get("reason").stringValue());
        assertEquals(firstToken, sender.next("game").get("token").stringValue()); // the current game
        clients.stream().filter(client -> client != sender).forEach(TestClient::assertNothingCame);
    }

    @Test
    void duringAnInterruptWindowTheOthersGetNoFrame() {
        val planning = new SkillCard(3, STRATEGIC_PLANNING);
        startGame(TestEngines.start(3, 7L, new ScriptEvent(all(
                all(new SelectCharacterEvent(1, WILLIAM_ADAMA), new SelectCharacterEvent(2, LAURA_ROSLIN),
                        new SelectCharacterEvent(3, KARA_STARBUCK_THRACE), new CreateDestinyDeckEvent()),
                all(new GiveSkillCardEvent(2, planning), new GiveSkillCardEvent(3, new SkillCard(4, STRATEGIC_PLANNING))),
                single(new KeepTabsOnVisitorFailEvent()),
                single(new CylonsWinEvent())))));
        val holder = clients.get(1);
        assertEquals("Decide", holder.game().get("input").get("kind").stringValue());
        assertEquals("Waiting", clients.get(0).game().get("input").get("kind").stringValue());

        holder.submit(codec.writeEvent(PlayInterruptCardEvent.play(2, planning)));

        holder.next("update");
        holder.next("game");
        // the next holder is asked in secret, and nobody else hears of it
        assertEquals("Decide", clients.get(2).nextGame().get("game").get("input").get("kind").stringValue());
        List.of(clients.get(0), clients.get(3)).forEach(TestClient::assertNothingCame);

        val last = clients.get(2);
        last.submit(codec.writeEvent(PlayInterruptCardEvent.pass(3)));
        for (val client : clients) {
            val game = client.nextGame();
            assertEquals("CYLONS", game.get("game").get("outcome").get("winner").stringValue());
        }
    }

    @Test
    void theServerRestartsInTheMiddleOfAGameAndTheClientsGoOn() {
        scriptedGame();
        val inMemory = ScriptedTurns.start();
        val inputs = ScriptedTurns.inputs();
        var before = clients.getFirst().token();
        for (val next : inputs.subList(0, 8)) {
            val input = next.apply(inMemory.view());
            input.applyTo(inMemory);
            val sender = clients.get(input.player() - 1);
            sender.awaitToken(before);
            before = send(sender, input);
        }

        clients.forEach(TestClient::close);
        try (val restarted = new SpringApplicationBuilder(FrakkinToastersApplication.class)
                .profiles("dev")
                .run("--server.port=0",
                        "--spring.datasource.url=" + postgres.getJdbcUrl(),
                        "--spring.datasource.username=" + postgres.getUsername(),
                        "--spring.datasource.password=" + postgres.getPassword())) {
            val restartedPort = Integer.parseInt(restarted.getEnvironment().getProperty("local.server.port"));
            val game = currentGame;
            // the logins survive, and so does the game
            for (val client : clients) {
                client.connect(game, restartedPort);
                client.next("game");
                client.next("log");
            }
            var last = clients.getFirst().token();
            for (val next : inputs.subList(8, inputs.size())) {
                val input = next.apply(inMemory.view());
                input.applyTo(inMemory);
                val sender = clients.get(input.player() - 1);
                sender.awaitToken(last);
                last = send(sender, input);
            }
            for (val client : clients) {
                client.awaitToken(last);
            }
            assertEquals(5, clients.getFirst().game().get("turn").get("number").intValue());
        }
    }

    @Test
    void aGameSavedByAnotherVersionIsRefusedWithTheReasonBeforeTheClose() {
        val game = scriptedGame();
        val client = clients.getFirst();
        client.close();
        val older = JournalCodec.FORMAT_VERSION - 1;
        jdbc.sql("UPDATE game_bundle SET format_version = :older WHERE id = :id")
                .param("older", older)
                .param("id", game)
                .update();
        sessions.evict(game);

        client.connect(game);

        val rejected = client.next("rejected");
        assertEquals("UNAVAILABLE", rejected.get("reason").stringValue());
        assertTrue(rejected.get("message").stringValue().contains("format " + older), rejected.toString());
        assertEquals(1008, client.awaitClose()); // the client doesn't reconnect after it
    }

    /* setup */

    private UUID currentGame;

    // The players join through the lobby, and the game starts with the scripted turns' seeded engine.
    private UUID scriptedGame() {
        return startGame(ScriptedTurns.start());
    }

    private UUID startGame(GameEngine engine) {
        val suffix = UUID.randomUUID().toString().substring(0, 6);
        for (val name : List.of("Adama", "Roslin", "Starbuck", "Dualla")) {
            val client = new TestClient(port);
            client.login(name + " " + suffix);
            clients.add(client);
        }
        val room = clients.get(0).post("/api/games", "{\"name\":\"Live " + suffix + "\"}");
        currentGame = UUID.fromString(room.get("id").stringValue());
        clients.get(1).post("/api/games/" + currentGame + "/join", "{\"as\":\"PLAYER\"}");
        clients.get(2).post("/api/games/" + currentGame + "/join", "{\"as\":\"PLAYER\"}");
        clients.get(3).post("/api/games/" + currentGame + "/join", "{\"as\":\"OBSERVER\"}");
        sessions.start(currentGame, engine);
        for (val client : clients) {
            client.connect(currentGame);
            client.next("game");
            client.next("log");
        }
        return currentGame;
    }

    /* helpers */

    // Sends the input and waits for the game that follows it, whose token it returns.
    private String send(TestClient client, ScriptedTurns.Input input) {
        if (input.submit() != null) {
            client.submit(codec.writeEvent(input.submit()));
        } else {
            val options = client.game().get("input").get("options");
            var index = -1;
            for (int i = 0; i < options.size(); i++) {
                if (options.get(i).equals(codec.values().write(input.choose(), xyz.zlatanov.frakkintoasters.event.Followup.class))) {
                    index = i;
                }
            }
            assertTrue(index >= 0, "the option is offered: " + options);
            client.choose(index);
        }
        return client.nextGame().get("token").stringValue();
    }



    // A player sees only his own cards. An observer (viewer 4) sees nobody's.
    private static void assertNoCardsOfOthers(TestClient client, int viewer) {
        for (val message : client.received()) {
            val type = message.get("type").stringValue();
            if (type.equals("game")) {
                for (val player : message.get("game").get("players")) {
                    if (player.get("number").intValue() == viewer) {
                        continue;
                    }
                    for (val pile : List.of("skillCards", "loyaltyCards", "motiveCards", "mutinyCards", "superCrisisCards")) {
                        assertTrue(player.get(pile).get("cards").isNull(), "player " + viewer + " sees " + pile + " of "
                                + player.get("number"));
                    }
                }
                for (val deck : message.get("game").get("decks")) {
                    assertTrue(deck.get("drawPile").isNull());
                }
            }
            if (type.equals("update") || type.equals("log")) {
                val entries = message.get(type).get("entries");
                for (val entry : entries) {
                    if (!entry.has("facts")) {
                        continue;
                    }
                    for (val fact : entry.get("facts")) {
                        if (!fact.get("kind").stringValue().equals("Shown")) {
                            continue;
                        }
                        val shown = fact.get("fact");
                        val kind = shown.get("kind").stringValue();
                        if (kind.equals("PlayerFact.CardsGained") || kind.equals("PlayerFact.CardsLost")
                                || kind.equals("PlayerFact.LoyaltyCardSeen") || kind.equals("PlayerFact.CardsSeen")) {
                            assertEquals(viewer, shown.get("playerNumber").intValue(), "player " + viewer + " sees " + shown);
                        }
                        assertNotEquals("DeckFact.CardsDrawn", kind);
                        assertNotEquals("DeckFact.DeckShuffled", kind);
                    }
                }
            }
        }
    }

}
