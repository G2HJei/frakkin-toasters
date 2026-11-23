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
import xyz.zlatanov.frakkintoasters.event.AnytimeEvent;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.event.sandbox.SandboxEvent;
import xyz.zlatanov.frakkintoasters.projection.GameProjection;
import xyz.zlatanov.frakkintoasters.projection.LiveUpdateProjection;
import xyz.zlatanov.frakkintoasters.projection.Viewer;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;
import xyz.zlatanov.frakkintoasters.state.exception.InvalidActionException;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static xyz.zlatanov.frakkintoasters.backend.game.CommandResult.accepted;
import static xyz.zlatanov.frakkintoasters.backend.game.CommandResult.rejected;

/**
 * Runs the commands of the games, one at a time per game. A command is a {@code submit} or a {@code choose} from a
 * player's connection:
 * <ol>
 *     <li>lock the game</li>
 *     <li>get the session: from the cache, or restored from the journal's rows</li>
 *     <li>check the token: a command that names an old state is stale</li>
 *     <li>run the engine, which changes nothing when it refuses the input or fails</li>
 *     <li>store the new entries and the summary in one transaction</li>
 *     <li>if that failed, evict the session, which is ahead of the stored journal</li>
 *     <li>publish what each viewer may see to the sink, still under the lock</li>
 *     <li>unlock</li>
 * </ol>
 */
@Slf4j
@Service
public class GameSessions {

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

    /**
     * @param takeOnlyAnswers every engine answers a decision with a single legal answer itself, see
     *                        {@link GameEngine#takeOnlyAnswers()}
     */
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

    /**
     * Starts the game of a lobby: the entries up to the first pause and the status {@code RUNNING}, in one transaction.
     */
    public void start(UUID gameBundleId, int playerCount) {
        start(gameBundleId, GameEngine.start(playerCount, randomness.next()));
    }

    /**
     * Starts the game with an engine that already ran to its first pause.
     */
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

    /**
     * @return the game as the viewer may see it, with the token for his next command
     * @throws GameNotAvailableException if the game can't be opened
     */
    public GameMessage status(UUID gameBundleId, Viewer viewer) {
        return withLock(gameBundleId, () -> handOut(session(gameBundleId), viewer));
    }

    /**
     * Hands a new connection what it gets first, the game and the whole log, under the game's lock, so it neither misses
     * a command nor gets one before them. The consumer must only queue, not send.
     *
     * @throws GameNotAvailableException if the game can't be opened
     */
    public void connect(UUID gameBundleId, Viewer viewer, Consumer<Frames> register) {
        withLock(gameBundleId, () -> {
            val session = session(gameBundleId);
            register.accept(new Frames(null, LiveUpdateProjection.of(session.journal().entries(), viewer),
                    handOut(session, viewer)));
            return null;
        });
    }

    /**
     * @return the whole log as the viewer may see it, with the game
     */
    public Frames history(UUID gameBundleId, Viewer viewer) {
        return withLock(gameBundleId, () -> {
            val session = session(gameBundleId);
            return new Frames(null, LiveUpdateProjection.of(session.journal().entries(), viewer), handOut(session, viewer));
        });
    }

    /**
     * Answers the pending decision with the event. The event is read only if its type fits the decision, and its
     * player is the viewer's. An {@link AnytimeEvent} answers nothing: it runs ahead of the pending input, whoever is
     * asked.
     */
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

    // A player's event is his; in a sandbox the event names the player who acts.
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

    /**
     * Picks an option of the pending choice by its index.
     */
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

    /**
     * Goes back to the pause before the last input of a sandbox: the journal rows after it are deleted, in the
     * transaction that updates the summary. Every connection then gets the game and the whole log again.
     */
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

    /**
     * Runs a sandbox setup event ahead of the pending input. The event is read only if its type is a setup event.
     */
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
            if (!tokens.matches(token, gameBundleId, session.sequence())) {
                return new Rejected(Reason.STALE, "The game has moved on", handOut(session, viewer));
            }
            return command.apply(session);
        });
    }

    /**
     * @return the journal of the game as it is stored, with what a restore needs
     */
    public Journal journal(UUID gameBundleId) {
        return withLock(gameBundleId, () -> session(gameBundleId).journal());
    }

    /**
     * Drops the game from the cache. The next command restores it from the journal's rows.
     */
    public void evict(UUID gameBundleId) {
        cache.invalidate(gameBundleId);
    }

    /* the steps of a command */

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
            if (!tokens.matches(token, gameBundleId, session.sequence())) {
                return new Rejected(Reason.STALE, "The game has moved on", handOut(session, viewer));
            }
            if (viewer instanceof Viewer.PlayerViewer(int player)) {
                return command.apply(session, player);
            }
            // the user of a sandbox answers for whoever is asked
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
        return new GameMessage(tokens.token(session.gameBundleId(), session.sequence()),
                GameProjection.of(session.view(), session.engine().pendingInput(), session.engine().pendingScope(), viewer));
    }

    // The viewer's connection gets the current game, which every other connection of his has already: so it counts as
    // published, and a command that changes nothing he may see sends him nothing.
    private GameMessage handOut(GameSession session, Viewer viewer) {
        val message = message(session, viewer);
        session.publish(viewer, message.game());
        return message;
    }

    /**
     * Hands what each viewer may see of the entries from the sequence on to the sink. A viewer for whom nothing visible
     * changed gets nothing, so a secret interrupt window stays silent for the others.
     *
     * @param replacesLog the viewers get the whole log instead of an update, e.g. after an undo
     */
    void publish(GameSession session, int fromSequence, boolean replacesLog) {
        val entries = session.journal().entries();
        val built = new HashMap<Viewer, Optional<Frames>>();
        if (replacesLog) {
            session.forgetPublished();
        }
        sinks.getIfAvailable(() -> ProjectionSink.NONE).publish(session.gameBundleId(), new Publication() {
            @Override
            public Optional<Frames> forViewer(Viewer viewer) {
                return built.computeIfAbsent(viewer, ignored -> frames(session, entries, fromSequence, viewer, replacesLog));
            }

            @Override
            public boolean replacesLog() {
                return replacesLog;
            }
        });
    }

    private Optional<Frames> frames(GameSession session, List<JournalEntry> entries,
                                    int fromSequence, Viewer viewer, boolean replacesLog) {
        val message = message(session, viewer);
        val previous = session.publish(viewer, message.game());
        if (replacesLog) {
            return Optional.of(new Frames(null, LiveUpdateProjection.of(entries, viewer), message));
        }
        val update = LiveUpdateProjection.of(entries, fromSequence, viewer);
        if (update.isEmpty() && message.game().equals(previous)) {
            return Optional.empty();
        }
        return Optional.of(new Frames(update.isEmpty() ? null : update, null, message));
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
