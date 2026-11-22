package xyz.zlatanov.frakkintoasters.fuzz;

import lombok.val;
import xyz.zlatanov.frakkintoasters.engine.GameEngine;
import xyz.zlatanov.frakkintoasters.engine.PendingInput;
import xyz.zlatanov.frakkintoasters.engine.ProcessorRegistry;
import xyz.zlatanov.frakkintoasters.engine.ScriptedTurns.Input;
import xyz.zlatanov.frakkintoasters.engine.TestEngines;
import xyz.zlatanov.frakkintoasters.engine.answer.LegalAnswers;
import xyz.zlatanov.frakkintoasters.engine.journal.Journal;
import xyz.zlatanov.frakkintoasters.engine.journal.JournalEntry;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.event.loyalty.RevealMotiveCardEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.fuzz.Finding.Kind;
import xyz.zlatanov.frakkintoasters.fuzz.Finding.Pause;
import xyz.zlatanov.frakkintoasters.fuzz.Repro.Expectation;
import xyz.zlatanov.frakkintoasters.state.GameStep;
import xyz.zlatanov.frakkintoasters.state.exception.InvalidActionException;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.TreeSet;
import java.util.function.Supplier;
import java.util.stream.IntStream;

/**
 * Plays one seeded game with random legal answers until it ends, a finding stops it or it runs past a cap. A few times
 * a game it restores the journal and gives the next input to both engines, which have to stay equal.
 */
public final class Playout {

    private static final int LAST_INPUTS = 200;

    /**
     * The trace and the repros change nothing in the game: a seed plays the same game with or without them.
     *
     * @param roundCap      the rounds a game may take
     * @param turnInputCap  the inputs one player turn may take (the setup is turn 0)
     * @param twins         how many times a game is restored on the way
     * @param anytimeChance 1 in how many pauses a player reveals a Motive Card he can
     * @param probeChance   1 in how many decisions first get an input from the wide domains, 0 for none
     * @param mutationChance 1 in how many answers are checked against their mutations (one component set to each of
     *                       its wide values), 0 for none
     * @param fullTrace     keep every input, not only the last ones
     * @param repro         keep the journal and the failing input of each finding, for a regression journal
     * @param firstEvent    the game's first event, {@code null} for a real start
     */
    public record Options(int roundCap, int turnInputCap, int twins, int anytimeChance, int probeChance,
                          int mutationChance, boolean fullTrace, boolean repro, Event firstEvent) {

        public static final int ROUND_CAP = 50;

        public static Options defaults() {
            return new Options(ROUND_CAP, 5_000, 3, 20, 20, 10, false, false, null);
        }

        public Options withRoundCap(int rounds) {
            return new Options(rounds, turnInputCap, twins, anytimeChance, probeChance, mutationChance, fullTrace, repro,
                    firstEvent);
        }

        public Options withProbeChance(int chance) {
            return new Options(roundCap, turnInputCap, twins, anytimeChance, chance, mutationChance, fullTrace, repro,
                    firstEvent);
        }

        public Options withMutationChance(int chance) {
            return new Options(roundCap, turnInputCap, twins, anytimeChance, probeChance, chance, fullTrace, repro,
                    firstEvent);
        }

        public Options withFullTrace() {
            return new Options(roundCap, turnInputCap, twins, anytimeChance, probeChance, mutationChance, true, repro,
                    firstEvent);
        }

        public Options withRepro() {
            return new Options(roundCap, turnInputCap, twins, anytimeChance, probeChance, mutationChance, fullTrace, true,
                    firstEvent);
        }

        public Options withFirstEvent(Event event) {
            return new Options(roundCap, turnInputCap, twins, anytimeChance, probeChance, mutationChance, fullTrace, repro,
                    event);
        }
    }

    // The game as it waited for an input: enough to say where a finding came from after the input broke the engine.
    private record Waiting(int turn, GameStep step, Optional<PendingInput> pending, int journalSize) {
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
    // The option whose decision is being looked at, so a candidate that crashes there can be given again.
    private Followup               lookingAt;

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
            stop(Finding.of(Kind.CRASH, thrown, "StartGameEvent"), () -> null);
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
            stop(Finding.of(Kind.NO_END, "round cap", "No ending after " + options.roundCap() + " rounds"),
                    () -> pauseNow(repro(0, null, null, null)));
            return true;
        }
        if (turnInputs > options.turnInputCap()) {
            val step = view.turn().step();
            stop(Finding.of(Kind.NO_END, "stuck in " + step,
                            "More than " + options.turnInputCap() + " inputs in one turn, in `" + step + "`"),
                    () -> pauseNow(repro(0, null, null, null)));
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
            val decision = LegalAnswers.decisionOf(option);
            if (decision == null) {
                return new Input(choice.player(), null, option);
            }
            lookingAt = option;
            val answer = AnswerFinder.find(engine.view(), decision, random, this::note);
            lookingAt = null;
            if (answer.isPresent()) {
                lookedAhead = decision;
                lookedAheadAnswer = answer.get();
                return new Input(choice.player(), null, option);
            }
            note(Finding.of(Kind.DEAD_OPTION, "decision " + describe(decision),
                            "An option leads to `" + describe(decision) + "`, which has no legal answer"),
                    () -> pauseNow(repro(choice.player(), option, null, Expectation.ANSWERS)));
            options.remove(option);
        }
        stop(Finding.of(Kind.NO_ANSWER, "every option dead",
                        "Every option of a choice leads to a decision without a legal answer").withDetail(choice.toString()),
                () -> pauseNow(repro(choice.player(), null, null, Expectation.ANSWERS)));
        return null;
    }

    private Input answer(PlayerDecisionEvent<?> decision) {
        LegalAnswers answers = null;
        if (options.probeChance() > 0 && random.nextInt(options.probeChance()) == 0) {
            answers = AnswerFinder.answers(engine.view(), decision, this::note);
            if (probe(decision, answers)) {
                lookedAhead = null;
                return null;
            }
        }
        if (decision.equals(lookedAhead)) {
            lookedAhead = null;
            return new Input(decision.playerNumber(), lookedAheadAnswer, null);
        }
        if (answers == null) {
            answers = AnswerFinder.answers(engine.view(), decision, this::note);
        }
        val answer = AnswerFinder.find(engine.view(), decision, answers, random, this::note);
        if (answer.isPresent() && options.mutationChance() > 0 && random.nextInt(options.mutationChance()) == 0) {
            val waiting = waiting();
            Mutations.check(engine.view(), decision, answers, answer.get(), random, finding -> note(finding,
                    () -> pause(waiting, repro(waiting, decision.playerNumber(), null, finding.event(), Expectation.RUNS))));
        }
        if (answer.isEmpty()) {
            stop(Finding.of(Kind.NO_ANSWER, "decision " + describe(decision),
                            "No legal answer to `" + describe(decision) + "`").withDetail(decision.toString()),
                    () -> pauseNow(repro(decision.playerNumber(), null, null, Expectation.ANSWERS)));
            return null;
        }
        return new Input(decision.playerNumber(), answer.get(), null);
    }

    // An input from the wide domains of a random class that may answer: the game has to reject it and stay as it was,
    // or take it and go on, and then the legal answers have to cover it. True when it took the place of the answer:
    // the game took it, or it ended the game.
    private boolean probe(PlayerDecisionEvent<?> decision, LegalAnswers answers) {
        val classes = LegalAnswers.answerTypes(decision.action());
        if (classes.isEmpty()) {
            return false;
        }
        val asked = new Asked(engine.view(), decision, random, this::note);
        val event = Candidates.hostile(asked, classes.get(random.nextInt(classes.size())));
        if (event == null) {
            return false;
        }
        val waiting = waiting();
        trace("t" + waiting.turn() + " " + waiting.step() + " p" + decision.playerNumber() + ": probe " + event);
        val result = Probe.submit(engine, decision.playerNumber(), event);
        if (result.finding() != null) {
            Supplier<Pause> pause = () -> pause(waiting,
                    repro(waiting, decision.playerNumber(), null, event, Expectation.RUNS));
            if (result.finding().fatal()) {
                stop(result.finding(), pause);
                return true;
            }
            note(result.finding(), pause);
        }
        if (result.taken()) {
            inputCount++;
            turnInputs++;
            if (!covers(answers, decision, event)) {
                note(Mutations.missing(event, null), () -> pause(waiting,
                        repro(waiting, decision.playerNumber(), null, event, Expectation.RUNS)));
            }
        }
        return result.taken();
    }

    private static boolean covers(LegalAnswers answers, PlayerDecisionEvent<?> decision, Event event) {
        try {
            return answers.covers(AnswerFinder.constrained(decision, event));
        } catch (RuntimeException thrown) {
            return true; // the game took it, so its constraints fit: a crash here is the check's own
        }
    }

    private static String describe(PlayerDecisionEvent<?> decision) {
        return decision.action().getSimpleName()
                + (decision.eventConstraints().isEmpty() ? "" : " " + decision.eventConstraints());
    }

    // A Cylon Leader may reveal a Motive Card at any pause, without being asked.
    private boolean revealMotiveCard() {
        val view = engine.view();
        val order = new ArrayList<>(IntStream.rangeClosed(1, players).boxed().toList());
        Collections.shuffle(order, random);
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
            note(Finding.of(Kind.INPUT_CRASH, thrown, event.getClass().getSimpleName()).withEvent(event));
            return false;
        }
    }

    // The input goes to the engine, and at a twin's pause also to a game restored from the journal.
    private void give(Input input) {
        val twin = twinDue() ? restore() : Optional.<GameEngine>empty();
        if (stoppedBy != null) {
            return;
        }
        val waiting = waiting();
        trace("t" + waiting.turn() + " " + waiting.step() + " p" + input.player() + ": "
                + (input.submit() != null ? "submit " + input.submit() : "choose " + input.choose()));
        val failure = tryApply(engine, input);
        if (failure != null) {
            val finding = failed(input, failure);
            stop(finding, () -> pause(waiting, finding.kind() == Kind.CRASH
                    ? repro(waiting, input.player(), input.choose(), input.submit(), Expectation.RUNS)
                    : null));
            return;
        }
        inputCount++;
        turnInputs++;
        if (twin.isPresent()) {
            val twinFailure = tryApply(twin.get(), input);
            Supplier<Pause> before = () -> pause(waiting,
                    repro(waiting, input.player(), input.choose(), input.submit(), Expectation.RESTORES));
            if (twinFailure != null) {
                stop(Finding.of(Kind.RESTORE, "twin failed", "A restored game failed an input the original took")
                        .withDetail(Finding.of(Kind.RESTORE, twinFailure, inputName(input)).detail()), before);
                return;
            }
            val differences = Twins.differences(engine, twin.get());
            if (!differences.isEmpty()) {
                stop(Finding.of(Kind.RESTORE, "twin differs", "A restored game differs after the same input")
                        .withDetail(String.join("\n", differences)), before);
                return;
            }
            engine = twin.get();
        }
    }

    // The first input at or after a twin's place gets one, so an input that isn't the agent's doesn't skip it.
    private boolean twinDue() {
        if (twinsAt.isEmpty() || inputCount < twinsAt.first()) {
            return false;
        }
        twinsAt.pollFirst();
        return true;
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
        return Finding.of(Kind.CRASH, thrown, name).withEvent(input.submit());
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

    // A journal that doesn't restore is a regression test by itself.
    private Optional<GameEngine> restore() {
        try {
            return Optional.of(GameEngine.restore(engine.journal(), seed));
        } catch (Throwable thrown) {
            stop(Finding.of(Kind.RESTORE, thrown, "restore"), () -> pauseNow(repro(0, null, null, null)));
            return Optional.empty();
        }
    }

    // A finished game has to be explained by an ending, and has to restore as it is.
    private void finish() {
        if (Endings.classify(engine.view()).isEmpty()) {
            stop(Finding.of(Kind.BAD_ENDING, "no ending for " + engine.view().outcome().orElse(null),
                            "The " + engine.view().outcome().orElse(null) + " won, and no ending explains it"),
                    () -> pauseNow(repro(0, null, null, null)));
            return;
        }
        val restored = restore();
        if (restored.isPresent()) {
            val differences = Twins.differences(engine, restored.get());
            if (!differences.isEmpty()) {
                stop(Finding.of(Kind.RESTORE, "finished game differs", "The finished game restores differently")
                        .withDetail(String.join("\n", differences)), () -> pauseNow(repro(0, null, null, null)));
            }
        }
    }

    // A finding of the answer finder: the candidate it names is the input to give again, after the option whose
    // decision was being looked at.
    private void note(Finding finding) {
        note(finding, () -> {
            val event = finding.event();
            if (finding.kind() != Kind.INPUT_CRASH || event == null) {
                return pauseNow(repro(0, null, null, null));
            }
            val player = event instanceof PlayerEvent playerEvent ? playerEvent.playerNumber() : askedPlayer();
            return pauseNow(repro(player, lookingAt, event, Expectation.RUNS));
        });
    }

    // The pause is worked out once per finding and game: it copies the inputs, and the journal for a repro.
    private void note(Finding finding, Supplier<Pause> pause) {
        if (!findings.containsKey(finding.id())) {
            findings.put(finding.id(), finding.at(pauseOrNone(pause)));
        }
    }

    private void stop(Finding finding, Supplier<Pause> pause) {
        stoppedBy = finding;
        note(finding, pause);
    }

    private static Pause pauseOrNone(Supplier<Pause> pause) {
        try {
            return pause.get();
        } catch (Throwable thrown) {
            return new Pause("unknown (" + thrown + ")", "", List.of(), null);
        }
    }

    private Waiting waiting() {
        val view = engine.view();
        return new Waiting(view.turn().number(), view.turn().step(), engine.pendingInput(), engine.journal().size());
    }

    private Pause pauseNow(Repro repro) {
        return pause(waiting(), repro);
    }

    private Pause pause(Waiting waiting, Repro repro) {
        return new Pause(where(waiting.turn(), waiting.step(), waiting.pending()),
                waiting.pending().map(Object::toString).orElse("none, the game has ended"), List.copyOf(inputs), repro);
    }

    static String where(int turn, GameStep step, Optional<PendingInput> pending) {
        val asked = pending.map(input -> switch (input) {
            case PendingInput.AwaitingDecision decision -> "player " + decision.decision().playerNumber()
                    + " asked for `" + describe(decision.decision()) + "`";
            case PendingInput.AwaitingChoice choice -> "player " + choice.player() + " chooses among "
                    + choice.options().size() + " options";
        }).orElse("the game has ended");
        return "Turn " + turn + ", `" + step + "`, " + asked;
    }

    private int askedPlayer() {
        return engine.pendingInput().map(input -> switch (input) {
            case PendingInput.AwaitingDecision decision -> decision.decision().playerNumber();
            case PendingInput.AwaitingChoice choice -> choice.player();
        }).orElse(0);
    }

    private Repro repro(int player, Followup option, Event event, Expectation expect) {
        return options.repro() ? repro(waiting(), player, option, event, expect) : null;
    }

    // The journal as it was at the pause: a failed input may have left more of it behind.
    private Repro repro(Waiting waiting, int player, Followup option, Event event, Expectation expect) {
        if (!options.repro()) {
            return null;
        }
        val journal = new Journal(engine.journal().entries().subList(0, waiting.journalSize()));
        return new Repro(journal, seed, player, option, event, expect);
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
