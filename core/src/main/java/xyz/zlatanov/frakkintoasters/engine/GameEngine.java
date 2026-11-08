package xyz.zlatanov.frakkintoasters.engine;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.engine.PendingInput.AwaitingChoice;
import xyz.zlatanov.frakkintoasters.engine.PendingInput.AwaitingDecision;
import xyz.zlatanov.frakkintoasters.engine.journal.FactRecorder;
import xyz.zlatanov.frakkintoasters.engine.journal.Journal;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.Followup.AllOf;
import xyz.zlatanov.frakkintoasters.event.Followup.None;
import xyz.zlatanov.frakkintoasters.event.Followup.OneOf;
import xyz.zlatanov.frakkintoasters.event.Followup.Single;
import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.event.constraint.ConstrainedEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.operation.GameOps;
import xyz.zlatanov.frakkintoasters.state.Die;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.Randomness;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;
import xyz.zlatanov.frakkintoasters.state.exception.InvalidActionException;
import xyz.zlatanov.frakkintoasters.state.view.GameView;

import java.lang.reflect.Modifier;
import java.util.Optional;
import java.util.stream.Stream;

import static xyz.zlatanov.frakkintoasters.engine.journal.Journal.ROOT_SCOPE;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;

public class GameEngine {

    private final Game    game;
    private final Journal journal = new Journal();
    private final GameOps ops;
    private final Agenda  agenda  = new Agenda();

    private FrakCallTheAdmiralException failure; // something threw after an input was accepted, so the game can't go on

    GameEngine(int playerCount, Randomness randomness, Event first) {
        game = Game.builder(playerCount)
                .randomness(randomness)
                .die(new Die(randomness))
                .build();
        ops = new GameOps(game, new FactRecorder(game, journal));
        advance(() -> agenda.push(single(first), ROOT_SCOPE));
    }

    public EngineStatus status(int player) {
        ensureRunning();
        val pendingInput = pending();
        if (pendingInput.isEmpty()) {
            return EngineStatus.finished();
        }
        val input = pendingInput.get();
        val secretlyWaitingOtherPlayer = input.secret() && input.player() != player;
        return secretlyWaitingOtherPlayer
                ? EngineStatus.waiting()
                : EngineStatus.waiting(input);
    }

    /**
     * Answers the pending decision, then runs the game to the next input.
     *
     * @throws InvalidActionException      if the event doesn't answer the decision or isn't valid. Nothing changes, and
     *                                     the decision stays pending
     * @throws FrakCallTheAdmiralException if anything fails after the event was accepted. The game stops
     */
    public void submit(int player, Event event) {
        ensureRunning();
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
     * @throws FrakCallTheAdmiralException if anything fails after the option was picked. The game stops
     */
    public void choose(int player, Followup option) {
        ensureRunning();
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

    public GameView view() {
        return game;
    }

    public Journal journal() {
        return journal;
    }

    private void advance(Runnable acceptedInput) {
        try {
            acceptedInput.run();
            runToNextInput();
        } catch (RuntimeException e) {
            failure = e instanceof FrakCallTheAdmiralException bug
                    ? bug
                    : new FrakCallTheAdmiralException("The game stopped: " + e.getMessage(), e);
            throw failure;
        }
    }

    private PlayerDecisionEvent<?> getDecision(int player, Event event, Agenda.Frame frame) {
        if (!(frame.followup() instanceof Single(PlayerDecisionEvent<?> decision))) {
            throw new InvalidActionException("No decision is pending");
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
                    requireAnswerable(decision);
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
        val followup = processor.run(ops);
        journal.closeScope(followup);
        agenda.push(followup, scope);
    }

    private Optional<PendingInput> pending() {
        return agenda.top().map(frame -> switch (frame.followup()) {
            case Single(PlayerDecisionEvent<?> decision) -> new AwaitingDecision(decision);
            case OneOf oneOf -> new AwaitingChoice(chooser(oneOf), oneOf.options());
            default -> throw new FrakCallTheAdmiralException("The game stopped in the middle of a run");
        });
    }

    // A PlayerEvent names the acting player, so the options name the chooser, if anyone. Decision 7 in engine-plan.md.
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

    // Waiting on a decision nobody can answer would wait forever.
    private static void requireAnswerable(PlayerDecisionEvent<?> decision) {
        val action = decision.action();
        val concrete = !action.isInterface() && !Modifier.isAbstract(action.getModifiers());
        if (concrete && !ProcessorRegistry.hasProcessor(action)) {
            throw new FrakCallTheAdmiralException("Nothing can answer " + decision + ", " + action.getSimpleName()
                    + " has no processor");
        }
        if (!decision.eventConstraints().isEmpty() && !ConstrainedEvent.class.isAssignableFrom(action)) {
            throw new FrakCallTheAdmiralException(action.getSimpleName() + " can't carry the constraints of " + decision);
        }
    }

    private void ensureRunning() {
        if (failure != null) {
            throw new FrakCallTheAdmiralException("The game stopped after an error", failure);
        }
    }
}
