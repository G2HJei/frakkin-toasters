package xyz.zlatanov.frakkintoasters.backend.sandbox;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.JsonNodeFactory;
import xyz.zlatanov.frakkintoasters.backend.game.GameBundle.GameMode;
import xyz.zlatanov.frakkintoasters.backend.game.GameBundles;
import xyz.zlatanov.frakkintoasters.backend.game.GameSessions;
import xyz.zlatanov.frakkintoasters.backend.journal.CodecException;
import xyz.zlatanov.frakkintoasters.backend.journal.JournalCodec;
import xyz.zlatanov.frakkintoasters.backend.lobby.Lobby;
import xyz.zlatanov.frakkintoasters.backend.lobby.Participants;
import xyz.zlatanov.frakkintoasters.engine.GameEngine;
import xyz.zlatanov.frakkintoasters.engine.journal.Journal;
import xyz.zlatanov.frakkintoasters.engine.journal.JournalEntry;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.GameFact.GameStarted;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Stored like any game, so a sandbox survives a restart, shows in the lobby and can be exported, and a seed reproduces
 * it.
 */
@Service
@RequiredArgsConstructor
public class Sandboxes {

    private final GameBundles         bundles;
    private final Participants        participants;
    private final GameSessions        sessions;
    private final JournalCodec        codec;
    private final TransactionTemplate transaction;

    public UUID start(UUID userId, int playerCount, Long seed, String name) {
        if (playerCount < Lobby.MIN_PLAYERS || playerCount > Lobby.MAX_PLAYERS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "A game has " + Lobby.MIN_PLAYERS + " to " + Lobby.MAX_PLAYERS + " players");
        }
        val actualSeed = seed != null ? seed : ThreadLocalRandom.current().nextLong(1, 1_000_000_000L);
        val engine = GameEngine.start(playerCount, actualSeed);
        return store(userId, name(name, "Sandbox, seed " + actualSeed), engine);
    }

    public JsonNode export(UUID gameBundleId, UUID userId) {
        val bundle = sandbox(gameBundleId, userId);
        val journal = sessions.journal(gameBundleId);
        val export = JsonNodeFactory.instance.objectNode();
        export.put("formatVersion", bundle.formatVersion());
        export.put("seed", bundle.seed());
        export.put("playerCount", playerCount(journal));
        val entries = export.putArray("entries");
        journal.entries().forEach(entry -> entries.add(codec.toJson(entry)));
        return export;
    }

    public UUID importJournal(UUID userId, JsonNode file, String name) {
        if (file == null || !file.isObject()) {
            throw invalid("Not an exported journal");
        }
        val version = file.path("formatVersion").asInt(-1);
        if (version != JournalCodec.FORMAT_VERSION) {
            throw invalid("The journal is in format " + version + ", this version of the game reads format "
                    + JournalCodec.FORMAT_VERSION);
        }
        val seedNode = file.get("seed");
        if (seedNode == null || !seedNode.isIntegralNumber()) {
            throw invalid("The journal has no seed");
        }
        val entriesNode = file.get("entries");
        if (entriesNode == null || !entriesNode.isArray()) {
            throw invalid("The journal has no entries");
        }
        final GameEngine engine;
        try {
            val entries = new ArrayList<JournalEntry>();
            entriesNode.forEach(entry -> entries.add(codec.fromJson(entry)));
            engine = GameEngine.restore(new Journal(entries), seedNode.longValue());
        } catch (CodecException | IllegalArgumentException e) {
            throw invalid("The journal can't be restored: " + e.getMessage());
        } catch (RuntimeException e) {
            throw invalid("The journal can't be restored: " + e);
        }
        return store(userId, name(name, "Imported sandbox, seed " + seedNode.longValue()), engine);
    }

    private UUID store(UUID userId, String name, GameEngine engine) {
        val id = transaction.execute(ignored -> {
            val created = bundles.createSandbox(name, userId, JournalCodec.FORMAT_VERSION);
            participants.add(created, userId, null);
            return created;
        });
        sessions.start(id, engine);
        return id;
    }

    private xyz.zlatanov.frakkintoasters.backend.game.GameBundle sandbox(UUID gameBundleId, UUID userId) {
        val bundle = bundles.find(gameBundleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No such game"));
        if (bundle.mode() != GameMode.SANDBOX) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only a sandbox can be exported");
        }
        if (participants.find(gameBundleId, userId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your sandbox");
        }
        return bundle;
    }

    private static int playerCount(Journal journal) {
        return journal.facts().stream()
                .filter(GameStarted.class::isInstance)
                .map(fact -> ((GameStarted) fact).playerCount())
                .findFirst()
                .orElseThrow();
    }

    private static String name(String given, String otherwise) {
        if (given == null || given.isBlank()) {
            return otherwise + " (" + LocalDate.now() + ")";
        }
        return given.strip().length() > 60 ? given.strip().substring(0, 60) : given.strip();
    }

    private static ResponseStatusException invalid(String reason) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, reason);
    }
}
