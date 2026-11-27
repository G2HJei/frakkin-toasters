package xyz.zlatanov.frakkintoasters.backend.game;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;
import xyz.zlatanov.frakkintoasters.backend.game.CommandResult.Reason;
import xyz.zlatanov.frakkintoasters.backend.game.CommandResult.Rejected;
import xyz.zlatanov.frakkintoasters.backend.game.GameBundle.EndReason;
import xyz.zlatanov.frakkintoasters.backend.game.GameBundle.GameStatus;
import xyz.zlatanov.frakkintoasters.backend.game.ProjectionSink.Frames;
import xyz.zlatanov.frakkintoasters.backend.game.ProjectionSink.GameMessage;
import xyz.zlatanov.frakkintoasters.backend.game.ProjectionSink.Publication;
import xyz.zlatanov.frakkintoasters.backend.journal.CodecException;
import xyz.zlatanov.frakkintoasters.backend.journal.JournalCodec;
import xyz.zlatanov.frakkintoasters.backend.journal.ValueCodec;
import xyz.zlatanov.frakkintoasters.engine.GameEngine;
import xyz.zlatanov.frakkintoasters.engine.PendingInput.AwaitingChoice;
import xyz.zlatanov.frakkintoasters.engine.PendingInput.AwaitingDecision;
import xyz.zlatanov.frakkintoasters.engine.journal.Journal;
import xyz.zlatanov.frakkintoasters.engine.journal.JournalEntry;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.GameFact;
import xyz.zlatanov.frakkintoasters.event.AnytimeEvent;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.event.sandbox.SandboxEvent;
import xyz.zlatanov.frakkintoasters.projection.GameProjection;
import xyz.zlatanov.frakkintoasters.projection.GameReport;
import xyz.zlatanov.frakkintoasters.projection.LiveUpdateProjection;
import xyz.zlatanov.frakkintoasters.projection.Viewer;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;
import xyz.zlatanov.frakkintoasters.state.exception.InvalidActionException;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static xyz.zlatanov.frakkintoasters.backend.game.CommandResult.accepted;
import static xyz.zlatanov.frakkintoasters.backend.game.CommandResult.rejected;

@Slf4j
@Service
public class GameSessions {

    static final String ABANDONED = "The game was abandoned";

    private final GameBundles                         bundles;
    private final JournalStore                        store;
    private final JournalCodec                        codec;
    private final StateTokens                         tokens;
    private final RandomnessSource                    randomness;
    private final TransactionTemplate                 transaction;
    private final ObjectProvider<ProjectionSink>      sinks;
    private final boolean                             takeOnlyAnswers;
    private final Cache<UUID, GameSession>            cache = Caffeine.newBuilder()
            .expireAfterAccess(Duration.ofMinutes(30))
            .build();
    private final ConcurrentHashMap<UUID, ReentrantLock> locks = new ConcurrentHashMap<>();

    public GameSessions(GameBundles bundles, JournalStore store, JournalCodec codec, StateTokens tokens,
                        RandomnessSource randomness, TransactionTemplate transaction,
                        ObjectProvider<ProjectionSink> sinks,
                        @Value("${frakkin.take-only-answers:true}") boolean takeOnlyAnswers) {
        this.bundles = bundles;
        this.store = store;
        this.codec = codec;
        this.tokens = tokens;
        this.randomness = randomness;
        this.transaction = transaction;
        this.sinks = sinks;
        this.takeOnlyAnswers = takeOnlyAnswers;
    }

    public void start(UUID gameBundleId, int playerCount) {
        start(gameBundleId, GameEngine.start(playerCount, randomness.next()));
    }

    public void start(UUID gameBundleId, GameEngine engine) {
        withLock(gameBundleId, () -> {
            val session = new GameSession(gameBundleId, withOptions(engine));
            transaction.executeWithoutResult(ignored -> {
                store.append(gameBundleId, 1, session.journal().entries());
                bundles.seed(gameBundleId, engine.seed().orElse(null));
                updateSummary(session);
            });
            cache.put(gameBundleId, session);
            publish(session, 0, false);
            return null;
        });
    }

    public GameMessage status(UUID gameBundleId, Viewer viewer) {
        return withLock(gameBundleId, () -> handOut(session(gameBundleId), viewer));
    }

    /**
     * Under the game's lock, so the connection neither misses a command nor gets one before them. The consumer must only
     * queue, not send.
     */
    public void connect(UUID gameBundleId, Viewer viewer, Consumer<Frames> register) {
        withLock(gameBundleId, () -> {
            val session = session(gameBundleId);
            register.accept(new Frames(null, LiveUpdateProjection.of(session.journal().entries(), viewer),
                    handOut(session, viewer), session.report().orElse(null)));
            return null;
        });
    }

    public Frames history(UUID gameBundleId, Viewer viewer) {
        return withLock(gameBundleId, () -> {
            val session = session(gameBundleId);
            return new Frames(null, LiveUpdateProjection.of(session.journal().entries(), viewer), handOut(session, viewer),
                    session.report().orElse(null));
        });
    }

    public CommandResult submit(UUID gameBundleId, Viewer viewer, String token, JsonNode event) {
        if (isAnytime(event)) {
            return anytime(gameBundleId, viewer, token, event);
        }
        return command(gameBundleId, viewer, token, (session, player) -> {
            if (!(session.engine().pendingInput().orElse(null) instanceof AwaitingDecision(var decision))
                    || decision.playerNumber() != player) {
                return rejected(Reason.INVALID, "No decision of yours is pending");
            }
            final Event answer;
            try {
                answer = decode(event, decision.action(), player);
            } catch (CodecException e) {
                return rejected(Reason.INVALID, e.getMessage());
            }
            return run(session, () -> session.engine().submit(player, answer));
        });
    }

    private CommandResult anytime(UUID gameBundleId, Viewer viewer, String token, JsonNode json) {
        return command(gameBundleId, viewer, token, (session, ignoredAsked) -> {
            final Event event;
            try {
                event = decode(json, AnytimeEvent.class,
                        viewer instanceof Viewer.PlayerViewer(int player) ? player : null);
            } catch (CodecException e) {
                return rejected(Reason.INVALID, e.getMessage());
            }
            val submitter = ((AnytimeEvent) event).playerNumber();
            return run(session, () -> session.engine().submit(submitter, event));
        });
    }

    private boolean isAnytime(JsonNode json) {
        return json != null && json.isObject() && json.hasNonNull(ValueCodec.KIND) && json.get(ValueCodec.KIND).isString()
                && codec.values().registry().typeOf(json.get(ValueCodec.KIND).stringValue())
                .filter(AnytimeEvent.class::isAssignableFrom)
                .isPresent();
    }

    public CommandResult choose(UUID gameBundleId, Viewer viewer, String token, int option) {
        return command(gameBundleId, viewer, token, (session, player) -> {
            if (!(session.engine().pendingInput().orElse(null) instanceof AwaitingChoice(int chooser, var options))
                    || chooser != player) {
                return rejected(Reason.INVALID, "No choice of yours is pending");
            }
            if (option < 0 || option >= options.size()) {
                return rejected(Reason.INVALID, "No option " + option);
            }
            val picked = List.copyOf(options).get(option);
            return run(session, () -> session.engine().choose(player, picked));
        });
    }

    public CommandResult undo(UUID gameBundleId, Viewer viewer, String token) {
        if (!(viewer instanceof Viewer.Sandbox)) {
            return rejected(Reason.INVALID, "Undo is only for a sandbox");
        }
        return sandboxCommand(gameBundleId, viewer, token, session -> {
            try {
                session.engine().undo();
            } catch (InvalidActionException e) {
                return rejected(Reason.INVALID, e.getMessage());
            }
            val id = session.gameBundleId();
            try {
                transaction.executeWithoutResult(ignored -> {
                    store.truncate(id, session.sequence());
                    updateSummary(session);
                });
            } catch (RuntimeException e) {
                log.error("Game {} couldn't undo to sequence {}", id, session.sequence(), e);
                cache.invalidate(id);
                return rejected(Reason.FAILURE, "The game couldn't be saved");
            }
            publish(session, session.sequence(), true);
            return accepted();
        });
    }

    public CommandResult inject(UUID gameBundleId, Viewer viewer, String token, JsonNode event) {
        if (!(viewer instanceof Viewer.Sandbox)) {
            return rejected(Reason.INVALID, "Setup is only for a sandbox");
        }
        return sandboxCommand(gameBundleId, viewer, token, session -> {
            final SandboxEvent setup;
            try {
                setup = (SandboxEvent) decode(event, SandboxEvent.class, null);
            } catch (CodecException e) {
                return rejected(Reason.INVALID, e.getMessage());
            }
            return run(session, () -> session.engine().inject(setup));
        });
    }

    private CommandResult sandboxCommand(UUID gameBundleId, Viewer viewer, String token,
                                         java.util.function.Function<GameSession, CommandResult> command) {
        return withLock(gameBundleId, () -> {
            final GameSession session;
            try {
                session = session(gameBundleId);
            } catch (GameNotAvailableException e) {
                return rejected(Reason.INVALID, e.getMessage());
            }
            if (session.closed()) {
                return rejected(Reason.INVALID, ABANDONED);
            }
            if (!tokens.matches(token, gameBundleId, session.sequence())) {
                return new Rejected(Reason.STALE, "The game has moved on", handOut(session, viewer));
            }
            return command.apply(session);
        });
    }

    /**
     * The engine stays as it was, so nothing is revealed.
     */
    public void abandon(UUID gameBundleId) {
        withLock(gameBundleId, () -> {
            final GameSession session;
            try {
                session = session(gameBundleId);
            } catch (GameNotAvailableException e) {
                bundles.end(gameBundleId, EndReason.ABANDONED);
                return null;
            }
            transaction.executeWithoutResult(ignored -> bundles.end(gameBundleId, EndReason.ABANDONED));
            session.close();
            publish(session, session.sequence(), false);
            return null;
        });
    }

    public void reconnect(UUID gameBundleId, Set<UUID> userIds) {
        sinks.getIfAvailable(() -> ProjectionSink.NONE).reconnect(gameBundleId, userIds);
    }

    public Journal journal(UUID gameBundleId) {
        return withLock(gameBundleId, () -> session(gameBundleId).journal());
    }

    public void evict(UUID gameBundleId) {
        cache.invalidate(gameBundleId);
    }

    private interface PlayerCommand {
        CommandResult apply(GameSession session, int player);
    }

    private CommandResult command(UUID gameBundleId, Viewer viewer, String token, PlayerCommand command) {
        if (viewer instanceof Viewer.Observer) {
            return rejected(Reason.INVALID, "An observer doesn't play");
        }
        return withLock(gameBundleId, () -> {
            final GameSession session;
            try {
                session = session(gameBundleId);
            } catch (GameNotAvailableException e) {
                return rejected(Reason.INVALID, e.getMessage());
            }
            if (session.closed()) {
                return rejected(Reason.INVALID, ABANDONED);
            }
            if (!tokens.matches(token, gameBundleId, session.sequence())) {
                return new Rejected(Reason.STALE, "The game has moved on", handOut(session, viewer));
            }
            if (viewer instanceof Viewer.PlayerViewer(int player)) {
                return command.apply(session, player);
            }
            val pending = session.engine().pendingInput();
            if (pending.isEmpty()) {
                return rejected(Reason.INVALID, "The game is over");
            }
            return command.apply(session, pending.get().player());
        });
    }

    private CommandResult run(GameSession session, Runnable input) {
        val id = session.gameBundleId();
        val before = session.sequence();
        try {
            input.run();
        } catch (InvalidActionException e) {
            return rejected(Reason.INVALID, e.getMessage());
        } catch (RuntimeException e) {
            // The engine rolled back: nothing changed, nothing to store.
            log.error("Game {} failed at sequence {}", id, before, e);
            return rejected(Reason.FAILURE, e instanceof FrakCallTheAdmiralException ? e.getMessage() : "The game failed");
        }
        val entries = session.journal().entries().subList(before, session.sequence());
        try {
            transaction.executeWithoutResult(ignored -> {
                store.append(id, before + 1, entries);
                updateSummary(session);
            });
        } catch (RuntimeException e) {
            // The engine is ahead of the stored journal, so the next command restores the game from the rows.
            log.error("Game {} couldn't store the entries from sequence {}", id, before + 1, e);
            cache.invalidate(id);
            return rejected(Reason.FAILURE, "The game couldn't be saved");
        }
        publish(session, before, false);
        return accepted();
    }

    private GameSession session(UUID gameBundleId) {
        val cached = cache.getIfPresent(gameBundleId);
        if (cached != null) {
            return cached;
        }
        val bundle = bundles.find(gameBundleId)
                .orElseThrow(() -> new GameNotAvailableException("No such game"));
        if (bundle.status() == GameStatus.LOBBY) {
            throw new GameNotAvailableException("The game hasn't started");
        }
        if (bundle.formatVersion() != JournalCodec.FORMAT_VERSION) {
            throw new GameNotAvailableException("The game was saved by another version (format " + bundle.formatVersion()
                    + "), this one reads format " + JournalCodec.FORMAT_VERSION);
        }
        val journal = store.load(gameBundleId);
        val engine = bundle.seed() == null ? GameEngine.restore(journal) : GameEngine.restore(journal, bundle.seed());
        val session = new GameSession(gameBundleId, withOptions(engine));
        if (bundle.abandoned()) {
            session.close();
        }
        cache.put(gameBundleId, session);
        return session;
    }

    // From the engine's next input on: the pause it stands at stays, so a restored game is still the stored one.
    private GameEngine withOptions(GameEngine engine) {
        return takeOnlyAnswers ? engine.takeOnlyAnswers() : engine;
    }

    // The type has to fit the decision before anything else is read, and the player comes from the connection.
    private Event decode(JsonNode json, Class<?> action, Integer player) {
        if (json == null || !json.isObject() || !json.hasNonNull(ValueCodec.KIND) || !json.get(ValueCodec.KIND).isString()) {
            throw new CodecException("An event needs a " + ValueCodec.KIND);
        }
        val kind = json.get(ValueCodec.KIND).stringValue();
        val type = codec.values().registry().typeOf(kind)
                .orElseThrow(() -> new CodecException("Unknown event " + kind));
        if (!action.isAssignableFrom(type) || !type.isRecord()) {
            throw new CodecException("The decision needs a " + action.getSimpleName() + ", not a " + kind);
        }
        val copy = (ObjectNode) json.deepCopy();
        if (player != null && PlayerEvent.class.isAssignableFrom(type)) {
            copy.put("playerNumber", player);
        }
        return codec.readEvent(copy, Event.class);
    }

    private void updateSummary(GameSession session) {
        val view = session.view();
        val outcome = view.outcome();
        bundles.updateSummary(session.gameBundleId(),
                outcome.isPresent() ? GameStatus.FINISHED : GameStatus.RUNNING,
                view.turn().number(),
                outcome.orElse(null),
                session.engine().pendingInput().map(input -> input.player()).orElse(null));
    }

    private GameMessage message(GameSession session, Viewer viewer) {
        val token = tokens.token(session.gameBundleId(), session.sequence());
        if (session.closed()) {
            val game = GameProjection.of(session.view(), Optional.empty(), Optional.empty(), viewer);
            return new GameMessage(token, withoutAnytime(game));
        }
        return new GameMessage(token,
                GameProjection.of(session.view(), session.engine().pendingInput(), session.engine().pendingScope(), viewer));
    }

    private static GameProjection withoutAnytime(GameProjection game) {
        return new GameProjection(game.viewer(), game.objective(), game.turn(), game.players(), game.titles(),
                game.galactica(), game.pegasus(), game.cylonFleet(), game.cylonSupply(), game.decks(),
                game.presidentHand(), game.quorumCardsInPlay(), game.skillCheck(), game.nukes(),
                game.crisisCardsInPlay(), game.destinations(), game.distanceTraveled(), game.removedCharacters(),
                game.destroyedShips(), null, List.of(), game.outcome());
    }

    // The viewer's connection gets the current game, which every other connection of his has already: so it counts as
    // published, and a command that changes nothing he may see sends him nothing.
    private GameMessage handOut(GameSession session, Viewer viewer) {
        val message = message(session, viewer);
        session.publish(viewer, message.game());
        return message;
    }

    /**
     * A viewer for whom nothing visible changed gets nothing, so a secret interrupt window stays silent for the others.
     */
    void publish(GameSession session, int fromSequence, boolean replacesLog) {
        val entries = session.journal().entries();
        val built = new HashMap<Viewer, Optional<Frames>>();
        if (replacesLog) {
            session.forgetPublished();
        }
        val report = replacesLog || endsIn(entries, fromSequence) ? session.report().orElse(null) : null;
        sinks.getIfAvailable(() -> ProjectionSink.NONE).publish(session.gameBundleId(), new Publication() {
            @Override
            public Optional<Frames> forViewer(Viewer viewer) {
                return built.computeIfAbsent(viewer,
                        ignored -> frames(session, entries, fromSequence, viewer, replacesLog, report));
            }

            @Override
            public boolean replacesLog() {
                return replacesLog;
            }
        });
    }

    private Optional<Frames> frames(GameSession session, List<JournalEntry> entries,
                                    int fromSequence, Viewer viewer, boolean replacesLog, GameReport report) {
        val message = message(session, viewer);
        val previous = session.publish(viewer, message.game());
        if (replacesLog) {
            return Optional.of(new Frames(null, LiveUpdateProjection.of(entries, viewer), message, report));
        }
        val update = LiveUpdateProjection.of(entries, fromSequence, viewer);
        if (update.isEmpty() && message.game().equals(previous) && report == null) {
            return Optional.empty();
        }
        return Optional.of(new Frames(update.isEmpty() ? null : update, null, message, report));
    }

    private static boolean endsIn(List<JournalEntry> entries, int fromSequence) {
        return entries.subList(Math.min(fromSequence, entries.size()), entries.size()).stream()
                .anyMatch(entry -> entry instanceof JournalEntry.FactRecorded(var ignored, GameFact.GameEnded ended));
    }

    <T> T withLock(UUID gameBundleId, Supplier<T> action) {
        val lock = locks.computeIfAbsent(gameBundleId, ignored -> new ReentrantLock());
        lock.lock();
        try {
            return action.get();
        } finally {
            lock.unlock();
        }
    }

    Optional<GameSession> cached(UUID gameBundleId) {
        return Optional.ofNullable(cache.getIfPresent(gameBundleId));
    }
}
