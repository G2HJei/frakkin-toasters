package xyz.zlatanov.frakkintoasters.fuzz;

import lombok.val;
import xyz.zlatanov.frakkintoasters.engine.EngineStatus;
import xyz.zlatanov.frakkintoasters.engine.GameEngine;
import xyz.zlatanov.frakkintoasters.engine.PendingInput;
import xyz.zlatanov.frakkintoasters.engine.ProcessorRegistry;
import xyz.zlatanov.frakkintoasters.engine.ScriptedTurns.Input;
import xyz.zlatanov.frakkintoasters.engine.TestEngines;
import xyz.zlatanov.frakkintoasters.engine.journal.GameSnapshot;
import xyz.zlatanov.frakkintoasters.engine.journal.JournalEntry;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.loyalty.RevealMotiveCardEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.fuzz.Finding.Kind;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.exception.InvalidActionException;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.TreeSet;
import java.util.stream.IntStream;

/**
 * Plays one seeded game with random legal answers until it ends, a finding stops it or it runs past a cap. A few times
 * a game it restores the journal and gives the next input to both engines, which have to stay equal.
 */
public final class Playout {

    private static final int LAST_INPUTS = 200;

    /**
     * @param roundCap      the rounds a game may take
     * @param turnInputCap  the inputs one player turn may take (the setup is turn 0)
     * @param twins         how many times a game is restored on the way
     * @param anytimeChance 1 in how many pauses a player reveals a Motive Card he can
     * @param fullTrace     keep every input, not only the last ones
     * @param firstEvent    the game's first event, {@code null} for a real start
     */
    public record Options(int roundCap, int turnInputCap, int twins, int anytimeChance, boolean fullTrace,
                          Event firstEvent) {

        public static Options defaults() {
            return new Options(50, 5_000, 3, 20, false, null);
        }

        public Options withRoundCap(int rounds) {
            return new Options(rounds, turnInputCap, twins, anytimeChance, fullTrace, firstEvent);
        }

        public Options withFullTrace() {
            return new Options(roundCap, turnInputCap, twins, anytimeChance, true, firstEvent);
        }

        public Options withFirstEvent(Event event) {
            return new Options(roundCap, turnInputCap, twins, anytimeChance, fullTrace, event);
        }
    }

    private final int                  players;
    private final long                 seed;
    private final Options              options;
    private final Random               random;
    private final Map<String, Finding> findings = new LinkedHashMap<>();
    private final Deque<String>        inputs   = new ArrayDeque<>();
    private final TreeSet<Integer>     twinsAt  = new TreeSet<>();

    private GameEngine engine;
    private Finding    stoppedBy;
    private int        inputCount;
    private int        turn       = -1;
    private int        turnInputs;
    // The answer found when a choice looked ahead at its decision, for when that decision is asked.
    private PlayerDecisionEvent<?> lookedAhead;
    private Event                  lookedAheadAnswer;

    private Playout(int players, long seed, Options options) {
        this.players = players;
        this.seed = seed;
        this.options = options;
        this.random = new Random(seed * 0x9E3779B97F4A7C15L + 0x5DEECE66DL);
        // Spread over a game's length: early, mid and late, so a long game gets each.
        var from = 1;
        for (int i = 0; i < options.twins(); i++) {
            val to = from * 4 + 50;
            twinsAt.add(from + random.nextInt(to - from));
            from = to;
        }
    }

    public static PlayoutResult play(int players, long seed) {
        return play(players, seed, Options.defaults());
    }

    public static PlayoutResult play(int players, long seed, Options options) {
        return new Playout(players, seed, options).run();
    }

    private PlayoutResult run() {
        try {
            engine = options.firstEvent() == null
                    ? GameEngine.start(players, seed)
                    : TestEngines.start(players, seed, options.firstEvent());
        } catch (Throwable thrown) {
            stop(Finding.of(Kind.CRASH, thrown, "StartGameEvent"));
            return result();
        }
        while (stoppedBy == null) {
            val pending = engine.pendingInput();
            if (pending.isEmpty()) {
                finish();
                break;
            }
            if (overACap()) {
                break;
            }
            if (options.anytimeChance() > 0 && random.nextInt(options.anytimeChance()) == 0 && revealMotiveCard()) {
                continue;
            }
            val input = next(pending.get());
            if (input != null) {
                give(input);
            }
        }
        return result();
    }

    private boolean overACap() {
        val view = engine.view();
        if (view.turn().number() != turn) {
            turn = view.turn().number();
            turnInputs = 0;
        }
        if (turn / players > options.roundCap()) {
            stop(Finding.of(Kind.NO_END, "round cap", "No ending after " + options.roundCap() + " rounds"));
            return true;
        }
        if (turnInputs > options.turnInputCap()) {
            val step = view.turn().step();
            stop(Finding.of(Kind.NO_END, "stuck in " + step,
                    "More than " + options.turnInputCap() + " inputs in one turn, in `" + step + "`"));
            return true;
        }
        return false;
    }

    private Input next(PendingInput pending) {
        return switch (pending) {
            case PendingInput.AwaitingChoice choice -> choose(choice);
            case PendingInput.AwaitingDecision decision -> answer(decision.decision());
        };
    }

    // A random option, but not one whose decision can't be answered: that is a finding of its own.
    private Input choose(PendingInput.AwaitingChoice choice) {
        val options = new ArrayList<>(choice.options());
        options.sort(Comparator.comparing(Followup::toString)); // a set's order may differ between runs
        while (!options.isEmpty()) {
            val option = options.get(random.nextInt(options.size()));
            val decision = firstDecision(option);
            if (decision == null) {
                return new Input(choice.player(), null, option);
            }
            val answer = AnswerFinder.find(engine.view(), decision, random, this::note);
            if (answer.isPresent()) {
                lookedAhead = decision;
                lookedAheadAnswer = answer.get();
                return new Input(choice.player(), null, option);
            }
            note(Finding.of(Kind.DEAD_OPTION, "decision " + describe(decision),
                    "An option leads to `" + describe(decision) + "`, which has no legal answer"));
            options.remove(option);
        }
        stop(Finding.of(Kind.NO_ANSWER, "every option dead",
                "Every option of a choice leads to a decision without a legal answer").withDetail(choice.toString()));
        return null;
    }

    private Input answer(PlayerDecisionEvent<?> decision) {
        if (decision.equals(lookedAhead)) {
            lookedAhead = null;
            return new Input(decision.playerNumber(), lookedAheadAnswer, null);
        }
        val answer = AnswerFinder.find(engine.view(), decision, random, this::note);
        if (answer.isEmpty()) {
            stop(Finding.of(Kind.NO_ANSWER, "decision " + describe(decision),
                    "No legal answer to `" + describe(decision) + "`").withDetail(decision.toString()));
            return null;
        }
        return new Input(decision.playerNumber(), answer.get(), null);
    }

    private static PlayerDecisionEvent<?> firstDecision(Followup followup) {
        return switch (followup) {
            case Followup.Single(PlayerDecisionEvent<?> decision) -> decision;
            case Followup.AllOf allOf when !allOf.followups().isEmpty() -> firstDecision(allOf.followups().getFirst());
            default -> null;
        };
    }

    private static String describe(PlayerDecisionEvent<?> decision) {
        return decision.action().getSimpleName()
                + (decision.eventConstraints().isEmpty() ? "" : " " + decision.eventConstraints());
    }

    // A Cylon Leader may reveal a Motive Card at any pause, without being asked.
    private boolean revealMotiveCard() {
        val view = engine.view();
        val order = new ArrayList<>(IntStream.rangeClosed(1, players).boxed().toList());
        java.util.Collections.shuffle(order, random);
        for (val playerNumber : order) {
            for (val card : view.player(playerNumber).motiveCards().cards().stream().distinct().toList()) {
                val event = new RevealMotiveCardEvent(playerNumber, card);
                if (acceptable(event)) {
                    give(new Input(playerNumber, event, null));
                    return true;
                }
            }
        }
        return false;
    }

    private boolean acceptable(Event event) {
        try {
            ProcessorRegistry.create(event).prepare(engine.view(), event);
            return true;
        } catch (InvalidActionException rejected) {
            return false;
        } catch (Throwable thrown) {
            note(Finding.of(Kind.INPUT_CRASH, thrown, event.getClass().getSimpleName()));
            return false;
        }
    }

    // The input goes to the engine, and at a twin's pause also to a game restored from the journal.
    private void give(Input input) {
        val twin = twinsAt.contains(inputCount) ? restore() : Optional.<GameEngine>empty();
        if (stoppedBy != null) {
            return;
        }
        val view = engine.view();
        trace("t" + view.turn().number() + " " + view.turn().step() + " p" + input.player() + ": "
                + (input.submit() != null ? "submit " + input.submit() : "choose " + input.choose()));
        val failure = tryApply(engine, input);
        if (failure != null) {
            stop(failed(input, failure));
            return;
        }
        inputCount++;
        turnInputs++;
        if (twin.isPresent()) {
            val twinFailure = tryApply(twin.get(), input);
            if (twinFailure != null) {
                stop(Finding.of(Kind.RESTORE, "twin failed", "A restored game failed an input the original took")
                        .withDetail(Finding.of(Kind.RESTORE, twinFailure, inputName(input)).detail()));
                return;
            }
            val differences = differences(engine, twin.get());
            if (!differences.isEmpty()) {
                stop(Finding.of(Kind.RESTORE, "twin differs", "A restored game differs after the same input")
                        .withDetail(String.join("\n", differences)));
                return;
            }
            engine = twin.get();
        }
    }

    // What the input threw, null when it was taken.
    private static Throwable tryApply(GameEngine target, Input input) {
        try {
            input.applyTo(target);
            return null;
        } catch (Throwable thrown) {
            return thrown;
        }
    }

    private static Finding failed(Input input, Throwable thrown) {
        val name = inputName(input);
        if (thrown instanceof InvalidActionException rejected) {
            // prepare accepted it, so the fuzzer and the engine check differently
            return Finding.of(Kind.FUZZER, "rejected " + name, "`submit` rejected a `" + name + "` that `prepare` accepted")
                    .withDetail(String.valueOf(rejected.getMessage()));
        }
        // A rollback, or an Error that got past it and left the engine half-applied: either way the game ends here.
        return Finding.of(Kind.CRASH, thrown, name);
    }

    private static String inputName(Input input) {
        return input.submit() != null ? input.submit().getClass().getSimpleName() : optionName(input.choose());
    }

    private static String optionName(Followup option) {
        return switch (option) {
            case Followup.Single single -> single.event().getClass().getSimpleName();
            case Followup.AllOf allOf when !allOf.followups().isEmpty() -> optionName(allOf.followups().getFirst());
            default -> option.getClass().getSimpleName();
        };
    }

    private Optional<GameEngine> restore() {
        try {
            return Optional.of(GameEngine.restore(engine.journal(), seed));
        } catch (Throwable thrown) {
            stop(Finding.of(Kind.RESTORE, thrown, "restore"));
            return Optional.empty();
        }
    }

    // A finished game has to be explained by an ending, and has to restore as it is.
    private void finish() {
        if (Endings.classify(engine.view()).isEmpty()) {
            stop(Finding.of(Kind.BAD_ENDING, "no ending for " + engine.view().outcome().orElse(null),
                    "The " + engine.view().outcome().orElse(null) + " won, and no ending explains it"));
            return;
        }
        val restored = restore();
        if (restored.isPresent()) {
            val differences = differences(engine, restored.get());
            if (!differences.isEmpty()) {
                stop(Finding.of(Kind.RESTORE, "finished game differs", "The finished game restores differently")
                        .withDetail(String.join("\n", differences)));
            }
        }
    }

    private List<String> differences(GameEngine original, GameEngine twin) {
        val result = new ArrayList<String>();
        val a = GameSnapshot.of((Game) original.view()).values();
        val b = GameSnapshot.of((Game) twin.view()).values();
        val keys = new TreeSet<>(a.keySet());
        keys.addAll(b.keySet());
        for (val key : keys) {
            if (!Objects.equals(a.get(key), b.get(key))) {
                result.add(key + ": " + a.get(key) + " <> " + b.get(key));
            }
        }
        for (int player = 1; player <= players; player++) {
            EngineStatus mine = original.status(player);
            EngineStatus theirs = twin.status(player);
            if (!mine.equals(theirs)) {
                result.add("status " + player + ": " + mine + " <> " + theirs);
            }
        }
        if (!original.pendingInput().equals(twin.pendingInput())) {
            result.add("pending: " + original.pendingInput() + " <> " + twin.pendingInput());
        }
        return result;
    }

    private void note(Finding finding) {
        findings.putIfAbsent(finding.id(), finding);
    }

    private void stop(Finding finding) {
        stoppedBy = finding;
        findings.putIfAbsent(finding.id(), finding);
    }

    private void trace(String line) {
        inputs.addLast(line);
        if (!options.fullTrace() && inputs.size() > LAST_INPUTS) {
            inputs.removeFirst();
        }
    }

    private PlayoutResult result() {
        val view = engine == null ? null : engine.view();
        val eventTypes = new TreeSet<String>();
        if (engine != null) {
            for (val entry : engine.journal().entries()) {
                if (entry instanceof JournalEntry.ScopeOpened opened && opened.event() != null) {
                    eventTypes.add(opened.event().getClass().getSimpleName());
                }
            }
        }
        val stop = stoppedBy == null ? null : findings.getOrDefault(stoppedBy.id(), stoppedBy);
        return new PlayoutResult(players, seed,
                view == null ? Optional.empty() : view.outcome(),
                view == null ? Optional.empty() : Endings.classify(view),
                view == null ? 0 : view.turn().number() / players,
                inputCount,
                stop,
                List.copyOf(findings.values()),
                List.copyOf(inputs),
                eventTypes);
    }
}
