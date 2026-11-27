package xyz.zlatanov.frakkintoasters.fuzz;

import lombok.SneakyThrows;
import lombok.val;
import xyz.zlatanov.frakkintoasters.engine.ProcessorRegistry;
import xyz.zlatanov.frakkintoasters.event.Event;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * The run behind {@code ./gradlew :core:fuzz}: seeded games on a thread pool, then the report and a trace per finding.
 * With {@code --seed} it plays that seed for each player count, keeps every input in the trace, and leaves the report
 * alone.
 */
public final class FuzzRun {

    record Arguments(int games, List<Integer> players, String playersText, long firstSeed, Long seed, int threads,
                     int rounds, Path report, Path traces) {

        static Arguments parse(String... args) {
            var games = 100;
            var players = "3-7";
            var firstSeed = 1L;
            Long seed = null;
            var threads = Math.max(1, Runtime.getRuntime().availableProcessors() - 1);
            var rounds = Playout.Options.ROUND_CAP;
            var report = Path.of("fuzzer-report.md");
            var traces = Path.of("core/build/fuzz");
            for (int i = 0; i < args.length; i += 2) {
                if (i + 1 >= args.length) {
                    throw new IllegalArgumentException("No value for " + args[i]);
                }
                val value = args[i + 1].strip();
                switch (args[i]) {
                    case "--games" -> games = Integer.parseInt(value);
                    case "--players" -> players = value;
                    case "--first-seed" -> firstSeed = Long.parseLong(value);
                    case "--seed" -> seed = Long.parseLong(value);
                    case "--threads" -> threads = Integer.parseInt(value);
                    case "--rounds" -> rounds = Integer.parseInt(value);
                    case "--report" -> report = Path.of(value);
                    case "--traces" -> traces = Path.of(value);
                    default -> throw new IllegalArgumentException("Unknown argument " + args[i]);
                }
            }
            if (games < 1 || threads < 1 || rounds < 1) {
                throw new IllegalArgumentException("--games, --threads and --rounds take 1 or more");
            }
            return new Arguments(games, playerCounts(players), players, firstSeed, seed, threads, rounds, report,
                    traces);
        }

        static List<Integer> playerCounts(String text) {
            val counts = new ArrayList<Integer>();
            for (val part : text.split(",")) {
                val range = part.strip().split("-");
                val from = Integer.parseInt(range[0].strip());
                val to = range.length > 1 ? Integer.parseInt(range[1].strip()) : from;
                IntStream.rangeClosed(from, to).forEach(counts::add);
            }
            if (counts.isEmpty() || counts.stream().anyMatch(count -> count < 3 || count > 7)) {
                throw new IllegalArgumentException("A game has 3 to 7 players: " + text);
            }
            return counts.stream().distinct().sorted().toList();
        }

        Playout.Options options() {
            return Playout.Options.defaults().withRoundCap(rounds);
        }
    }

    private FuzzRun() {
    }

    public static void main(String[] args) {
        val arguments = Arguments.parse(args);
        // ClasspathScanner opens a zip file system per jar, which two threads can't do at once: scan before the pool.
        ProductionEvents.types();
        ProcessorRegistry.hasProcessor(Event.class);
        if (arguments.seed() != null) {
            single(arguments);
        } else {
            batch(arguments);
        }
    }

    @SneakyThrows
    private static void single(Arguments arguments) {
        Files.createDirectories(arguments.traces());
        for (val players : arguments.players()) {
            val game = play(players, arguments.seed(), arguments.options().withFullTrace());
            val gameTrace = arguments.traces().resolve(players + "p-" + arguments.seed() + ".txt");
            write(gameTrace, Traces.ofGame(game, arguments.rounds()));
            System.out.println(players + " players, seed " + arguments.seed() + ": " + Traces.outcome(game) + ", "
                    + game.rounds() + " rounds, " + game.inputCount() + " inputs. Trace: " + gameTrace);
            for (val finding : game.findings()) {
                val trace = arguments.traces().resolve(FuzzReport.traceName(finding, game));
                write(trace, Traces.ofFinding(game, finding, arguments.rounds()));
                System.out.println("  " + finding.id() + " " + finding.kind() + ": " + finding.summary() + " ("
                        + Optional.ofNullable(finding.pause()).map(Finding.Pause::where).orElse("") + "). Trace: "
                        + trace);
            }
        }
    }

    @SneakyThrows
    private static void batch(Arguments arguments) {
        cleanTraces(arguments.traces());
        val started = LocalDateTime.now();
        val start = System.nanoTime();
        val pool = Executors.newFixedThreadPool(arguments.threads());
        val completion = new ExecutorCompletionService<PlayoutResult>(pool);
        var tasks = 0;
        for (val players : arguments.players()) {
            for (long seed = arguments.firstSeed(); seed < arguments.firstSeed() + arguments.games(); seed++) {
                val gameSeed = seed;
                completion.submit(() -> play(players, gameSeed, arguments.options()));
                tasks++;
            }
        }
        System.out.println("fuzz: " + tasks + " games, " + arguments.threads() + " threads");
        val results = new RunResults();
        var lastReport = System.nanoTime();
        for (int done = 1; done <= tasks; done++) {
            results.add(completion.take().get());
            val now = System.nanoTime();
            if (done == tasks || done % Math.max(1, tasks / 10) == 0 || now - lastReport > 30_000_000_000L) {
                lastReport = now;
                System.out.println("fuzz: " + done + "/" + tasks + " games, " + results.failed() + " failed, "
                        + FuzzReport.duration(Duration.ofNanos(now - start)));
            }
        }
        pool.shutdown();
        val report = arguments.report().toAbsolutePath();
        val root = report.getParent();
        val run = new FuzzReport.Run(started, Duration.ofNanos(System.nanoTime() - start), commit(root),
                changedFiles(root), arguments.firstSeed(), arguments.games(), arguments.playersText(),
                arguments.rounds(), root.relativize(arguments.traces().toAbsolutePath()).toString().replace('\\', '/'));
        FuzzReport.write(report, run, results);
        Files.createDirectories(arguments.traces());
        for (val finding : results.findings()) {
            val game = results.example(finding.id());
            write(arguments.traces().resolve(FuzzReport.traceName(finding, game)),
                    Traces.ofFinding(game, finding, arguments.rounds()));
        }
        System.out.println("fuzz: " + tasks + " games in " + FuzzReport.duration(run.duration()) + ", "
                + results.failed() + " failed, " + results.findings().size() + " findings: " + report);
    }

    // A playout catches what the engine throws; this is for what the fuzzer itself throws.
    private static PlayoutResult play(int players, long seed, Playout.Options options) {
        try {
            return Playout.play(players, seed, options);
        } catch (Throwable thrown) {
            val finding = Finding.of(Finding.Kind.FUZZER, thrown, "Playout");
            return new PlayoutResult(players, seed, Optional.empty(), Optional.empty(), 0, 0, finding,
                    List.of(finding), List.of(), Set.of());
        }
    }

    @SneakyThrows
    private static void cleanTraces(Path traces) {
        if (!Files.isDirectory(traces)) {
            return;
        }
        try (Stream<Path> files = Files.list(traces)) {
            for (val file : files.filter(file -> file.toString().endsWith(".txt")).toList()) {
                Files.delete(file);
            }
        }
    }

    @SneakyThrows
    private static void write(Path file, String text) {
        Files.writeString(file, text, StandardCharsets.UTF_8);
    }

    private static String commit(Path root) {
        return git(root, "rev-parse", "--short", "HEAD").stream().findFirst().orElse("unknown");
    }

    // The count of the fix loop's 32-file rule: every changed path but the report's.
    private static int changedFiles(Path root) {
        val lines = git(root, "status", "--porcelain", "--untracked-files=all");
        return (int) lines.stream().filter(line -> !line.endsWith(" fuzzer-report.md")).count();
    }

    private static List<String> git(Path root, String... args) {
        val command = new ArrayList<String>();
        command.add("git");
        command.addAll(List.of(args));
        try {
            val process = new ProcessBuilder(command).directory(root.toFile()).redirectErrorStream(true).start();
            try (val reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                val lines = reader.lines().filter(line -> !line.isBlank()).toList();
                return process.waitFor() == 0 ? lines : List.of();
            }
        } catch (Exception e) {
            return List.of();
        }
    }
}
