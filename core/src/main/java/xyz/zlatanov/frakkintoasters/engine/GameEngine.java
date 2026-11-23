package xyz.zlatanov.frakkintoasters.engine;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.engine.PendingInput.AwaitingChoice;
import xyz.zlatanov.frakkintoasters.engine.PendingInput.AwaitingDecision;
import xyz.zlatanov.frakkintoasters.engine.journal.FactRecorder;
import xyz.zlatanov.frakkintoasters.engine.journal.GameReplayer;
import xyz.zlatanov.frakkintoasters.engine.journal.Journal;
import xyz.zlatanov.frakkintoasters.engine.journal.JournalEntry;
import xyz.zlatanov.frakkintoasters.event.AnytimeEvent;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.Followup.AllOf;
import xyz.zlatanov.frakkintoasters.event.Followup.None;
import xyz.zlatanov.frakkintoasters.event.Followup.OneOf;
import xyz.zlatanov.frakkintoasters.event.Followup.Single;
import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.event.constraint.ConstrainedEvent;
import xyz.zlatanov.frakkintoasters.event.character.AbilityTriggers;
import xyz.zlatanov.frakkintoasters.event.endgame.WinChecks;
import xyz.zlatanov.frakkintoasters.event.sandbox.SandboxEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.turn.StartGameEvent;
import xyz.zlatanov.frakkintoasters.operation.GameOps;
import xyz.zlatanov.frakkintoasters.state.Die;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.Randomness;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;
import xyz.zlatanov.frakkintoasters.state.exception.InvalidActionException;
import xyz.zlatanov.frakkintoasters.state.view.GameView;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;
import java.util.stream.Stream;

import static xyz.zlatanov.frakkintoasters.engine.journal.Journal.ROOT_SCOPE;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.allOf;

public class GameEngine {

    // A seeded engine reseeds its randomness before each input, from the seed and the size of the journal, so the same
    // inputs give the same game, also after a restore. Without a seed, a restored game gets fresh randomness.
    private final Long       seed;
    private final Randomness randomness;

    // A rollback replaces these, see advance().
    private Journal journal;
    private Game    game;
    private GameOps ops;
    private Agenda  agenda;
    // where each input began in the journal, so undo goes back to the pause before the last one
    private Deque<Integer> inputStarts = new ArrayDeque<>();

    GameEngine(int playerCount, Randomness randomness, Event first) {
        this(playerCount, null, randomness, first);
    }

    GameEngine(int playerCount, long seed, Event first) {
        this(playerCount, seed, Randomness.seeded(seed), first);
    }

    private GameEngine(int playerCount, Long seed, Randomness randomness, Event first) {
        this.seed = seed;
        this.randomness = randomness;
        journal = new Journal();
        reseed();
        game = Game.builder(playerCount)
                .randomness(randomness)
                .die(new Die(randomness))
                .build();
        ops = new GameOps(game, new FactRecorder(game, journal));
        agenda = new Agenda();
        agenda.push(single(first), ROOT_SCOPE);
        runToNextInput();
    }

    private GameEngine(Journal journal, Long seed) {
        this.seed = seed;
        this.randomness = seed == null ? null : Randomness.seeded(seed);
        rebuild(journal);
    }

    public static GameEngine start(int playerCount, Randomness randomness) {
        return new GameEngine(playerCount, randomness, new StartGameEvent());
    }

    /**
     * Starts a game whose randomness comes from the seed alone: the same seed and the same inputs give the same game,
     * also across {@link #restore(Journal, long)} and {@link #undo()}.
     */
    public static GameEngine start(int playerCount, long seed) {
        return new GameEngine(playerCount, seed, new StartGameEvent());
    }

    /**
     * Rebuilds a game from its journal. The engine gets a copy of the journal, so the two games go on independently. The
     * facts hold every random result, so the restored game needs no randomness of the original.
     *
     * @throws IllegalArgumentException if the journal doesn't start with {@code GameStarted}, doesn't match the moves of
     *                                  the run loop or ends neither at a pause nor with the end of the game
     */
    public static GameEngine restore(Journal journal) {
        return new GameEngine(new Journal(journal.entries()), null);
    }

    /**
     * Rebuilds a seeded game from its journal. It goes on as the original would with the same inputs.
     */
    public static GameEngine restore(Journal journal, long seed) {
        return new GameEngine(new Journal(journal.entries()), seed);
    }

    /**
     * @return the seed of a seeded game
     */
    public Optional<Long> seed() {
        return Optional.ofNullable(seed);
    }

    public EngineStatus status(int player) {
        val pendingInput = pendingInput();
        if (pendingInput.isEmpty()) {
            // runToNextInput leaves nothing pending only once the game has ended
            return EngineStatus.finished(game.outcome().orElseThrow(FrakCallTheAdmiralException::new));
        }
        val input = pendingInput.get();
        val secretlyWaitingOtherPlayer = input.secret() && input.player() != player;
        return secretlyWaitingOtherPlayer
                ? EngineStatus.waiting()
                : EngineStatus.waiting(input);
    }

    /**
     * Answers the pending decision, then runs the game to the next input. An {@link AnytimeEvent} answers nothing: it
     * runs ahead of the pending input, a decision or a choice, which stays pending.
     *
     * @throws InvalidActionException      if the event doesn't answer the decision or isn't valid. Nothing changes, and
     *                                     the decision stays pending
     * @throws FrakCallTheAdmiralException if anything fails after the event was accepted. The game rolls back to before
     *                                     the call, and the decision stays pending
     */
    public void submit(int player, Event event) {
        if (event instanceof AnytimeEvent anytime) {
            if (anytime.playerNumber() != player) {
                throw new InvalidActionException("Player " + player + " can't submit for player " + anytime.playerNumber());
            }
            runAhead(anytime);
            return;
        }
        val frame = agenda.top().orElseThrow(() -> new InvalidActionException("The game is over"));
        val decision = getDecision(player, event, frame);
        val answerEvent = event instanceof ConstrainedEvent constrained
                ? constrained.withConstraints(decision.eventConstraints())
                : event;
        val processor = ProcessorRegistry.create(answerEvent);
        processor.prepare(game, answerEvent);
        advance(() -> {
            agenda.take();
            run(processor, answerEvent, player, frame.parentScope());
        });
    }

    /**
     * Picks an option of the pending choice, then runs the game to the next input.
     *
     * @throws InvalidActionException      if there is no such option for the player. Nothing changes, and the choice
     *                                     stays pending
     * @throws FrakCallTheAdmiralException if anything fails after the option was picked. The game rolls back to before
     *                                     the call, and the choice stays pending
     */
    public void choose(int player, Followup option) {
        val frame = agenda.top().orElseThrow(() -> new InvalidActionException("The game is over"));
        if (!(frame.followup() instanceof OneOf oneOf)) {
            throw new InvalidActionException("No choice is pending");
        }
        val chooser = chooser(oneOf);
        if (player != chooser) {
            throw new InvalidActionException("Player " + chooser + " has to choose");
        }
        if (!oneOf.options().contains(option)) {
            throw new InvalidActionException("Not an option: " + option);
        }
        advance(() -> {
            journal.recordChoice(frame.parentScope(), chooser, option);
            agenda.select(option);
        });
    }

    /**
     * Runs a sandbox setup event at once, ahead of the pending input, then runs the game to the next input, with the
     * win checks of the run loop. A pending choice keeps the options it was built with, even when the setup makes them
     * stale.
     *
     * @throws InvalidActionException      if the game is over or the event isn't valid. Nothing changes
     * @throws FrakCallTheAdmiralException if anything fails after the event was accepted. The game rolls back
     */
    public void inject(SandboxEvent event) {
        runAhead(event);
    }

    /**
     * Goes back to the pause before the last input: a decision answered, an option picked, an event injected or an
     * {@link AnytimeEvent}. A
     * choice that led to a decision is undone on its own, and a finished game can be undone too.
     *
     * @throws InvalidActionException if there was no input yet
     */
    public void undo() {
        if (inputStarts.isEmpty()) {
            throw new InvalidActionException("Nothing to undo");
        }
        rebuild(new Journal(journal.entries().subList(0, inputStarts.peekLast())));
    }

    // An injected setup event or an anytime event: it runs at once, ahead of the pending input, with the win checks of
    // the run loop. AgendaReplayer pushes it again from the EventInjected entry, and undo takes it back on its own.
    private void runAhead(Event event) {
        if (agenda.isEmpty()) {
            throw new InvalidActionException("The game is over");
        }
        ProcessorRegistry.create(event).prepare(game, event);
        advance(() -> {
            journal.recordInjection(event);
            agenda.push(single(event), ROOT_SCOPE);
        });
    }

    /**
     * @return the game. A rollback replaces it, so ask again after a {@link FrakCallTheAdmiralException}
     */
    public GameView view() {
        return game;
    }

    /**
     * @return the journal. A rollback replaces it, so ask again after a {@link FrakCallTheAdmiralException}
     */
    public Journal journal() {
        return journal;
    }

    // All or nothing. Facts apply as they are recorded, so a failure rebuilds the game
    // from the journal as it stood before the input. Only a failure pays for the replay.
    private void advance(Runnable acceptedInput) {
        val entriesBefore = journal.size();
        reseed();
        try {
            acceptedInput.run();
            runToNextInput();
            inputStarts.addLast(entriesBefore);
        } catch (RuntimeException e) {
            rebuild(new Journal(journal.entries().subList(0, entriesBefore)));
            throw e instanceof FrakCallTheAdmiralException bug
                    ? bug
                    : new FrakCallTheAdmiralException("Rolled back: " + e.getMessage(), e);
        }
    }

    // The same seed and journal size give the same random results, however the engine got there.
    private void reseed() {
        if (seed == null) {
            return;
        }
        var mixed = seed + journal.size() * 0x9E3779B97F4A7C15L;
        mixed = (mixed ^ (mixed >>> 30)) * 0xBF58476D1CE4E5B9L;
        mixed = (mixed ^ (mixed >>> 27)) * 0x94D049BB133111EBL;
        randomness.reseed(mixed ^ (mixed >>> 31));
    }

    private void rebuild(Journal journal) {
        val rebuiltGame = seed == null ? GameReplayer.replay(journal) : GameReplayer.replay(journal, randomness);
        val replay = AgendaReplayer.replayWithInputs(journal);
        this.journal = journal;
        game = rebuiltGame;
        ops = new GameOps(game, new FactRecorder(game, journal));
        agenda = replay.agenda();
        inputStarts = new ArrayDeque<>(replay.inputStarts());
    }

    private PlayerDecisionEvent<?> getDecision(int player, Event event, Agenda.Frame frame) {
        if (!(frame.followup() instanceof Single(PlayerDecisionEvent<?> decision))) {
            throw new InvalidActionException("No decision is pending");
        }
        if (event instanceof SandboxEvent) {
            throw new InvalidActionException("A setup event is injected, not submitted");
        }
        if (player != decision.playerNumber()) {
            throw new InvalidActionException("Player " + decision.playerNumber() + " has to decide");
        }
        if (!decision.action().isInstance(event)) {
            throw new InvalidActionException("The decision needs a " + decision.action().getSimpleName());
        }
        if (event instanceof PlayerEvent playerEvent && playerEvent.playerNumber() != player) {
            throw new InvalidActionException("Player " + player + " can't submit for player " + playerEvent.playerNumber());
        }
        return decision;
    }

    private void runToNextInput() {
        while (!agenda.isEmpty()) {
            val frame = agenda.top().orElseThrow();
            switch (frame.followup()) {
                case Single(PlayerDecisionEvent<?> decision) -> {
                    requireCarriesConstraints(decision);
                    return;
                }
                case Single(Event event) -> runQueued(event, frame.parentScope());
                case OneOf oneOf -> {
                    chooser(oneOf); // fails now, not when someone asks for the status
                    return;
                }
                default -> throw new FrakCallTheAdmiralException("The agenda holds only events and choices");
            }
        }
        // Only the end of the game empties the agenda, every turn queues the next one.
        if (game.outcome().isEmpty()) {
            throw new FrakCallTheAdmiralException("Nothing is left to run, but the game goes on");
        }
    }

    // A processor put a queued event there, so if it doesn't run, that's a bug.
    private void runQueued(Event event, int parentScope) {
        val processor = ProcessorRegistry.create(event);
        try {
            processor.prepare(game, event);
        } catch (InvalidActionException e) {
            throw new FrakCallTheAdmiralException("The queued " + event + " is invalid", e);
        }
        agenda.take();
        run(processor, event, ScopeActor.ofQueued(game, event), parentScope);
    }

    private void run(EventProcessor<?> processor, Event event, int actor, int parentScope) {
        val scope = journal.openScope(event, actor, parentScope);
        val queued = processor.run(ops);
        // A win runs before what the event queued, then the abilities it triggered ("Memento"). The journal records
        // them all, so restore never runs the checks.
        val followup = allOf(WinChecks.check(game), AbilityTriggers.check(game), queued);
        journal.closeScope(followup);
        agenda.push(followup, scope);
        if (game.outcome().isPresent()) {
            agenda.clear(); // AgendaReplayer does the same at the scope that ended the game
        }
    }

    /**
     * @return the scope of the event that queued the pending input, e.g. the Crisis Card whose section is chosen. Empty
     * once the game has ended, and for an input queued by no event
     */
    public Optional<JournalEntry.ScopeOpened> pendingScope() {
        return agenda.top().flatMap(frame -> journal.scope(frame.parentScope()));
    }

    /**
     * @return what the game waits for, a secret input included. Empty once the game has ended
     */
    public Optional<PendingInput> pendingInput() {
        return agenda.top().map(frame -> switch (frame.followup()) {
            case Single(PlayerDecisionEvent<?> decision) -> new AwaitingDecision(decision);
            case OneOf oneOf -> new AwaitingChoice(chooser(oneOf), oneOf.options());
            default -> throw new FrakCallTheAdmiralException("The game stopped in the middle of a run");
        });
    }

    // A PlayerEvent names the acting player, so the options name the chooser, if anyone.
    private int chooser(OneOf oneOf) {
        val players = playersIn(oneOf).distinct().toList();
        if (players.size() > 1) {
            throw new FrakCallTheAdmiralException("The options of " + oneOf + " name players " + players);
        }
        return players.isEmpty() ? game.turn().currentPlayerNumber() : players.getFirst();
    }

    private static Stream<Integer> playersIn(Followup followup) {
        return switch (followup) {
            case None ignored -> Stream.empty();
            case Single(PlayerEvent event) -> Stream.of(event.playerNumber());
            case Single ignored -> Stream.empty();
            case AllOf all -> all.followups().stream().flatMap(GameEngine::playersIn);
            case OneOf oneOf -> oneOf.options().stream().flatMap(GameEngine::playersIn);
        };
    }

    // submit() copies the constraints onto the answer, so an answer that can't carry them would drop them.
    private static void requireCarriesConstraints(PlayerDecisionEvent<?> decision) {
        val action = decision.action();
        if (!decision.eventConstraints().isEmpty() && !ConstrainedEvent.class.isAssignableFrom(action)) {
            throw new FrakCallTheAdmiralException(action.getSimpleName() + " can't carry the constraints of " + decision);
        }
    }
}
