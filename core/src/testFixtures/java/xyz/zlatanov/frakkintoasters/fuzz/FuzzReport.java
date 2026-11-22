package xyz.zlatanov.frakkintoasters.fuzz;

import lombok.SneakyThrows;
import lombok.val;
import xyz.zlatanov.frakkintoasters.fuzz.Endings.Ending;
import xyz.zlatanov.frakkintoasters.state.Team;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * {@code fuzzer-report.md}. A run rewrites the block between the markers and keeps the history rows it finds there;
 * "Fixes" and "Questions" below it are written by hand, and the IDs under them give a finding its status.
 */
public final class FuzzReport {

    static final String BEGIN = "<!-- fuzz:begin -->";
    static final String END   = "<!-- fuzz:end -->";

    private static final String            HEADER   = """
            # Fuzzer report

            Random games played by `./gradlew :core:fuzz`. The block between the markers is rewritten by every run;
            "Fixes" and "Questions" are written by hand.

            """;
    private static final String            SECTIONS = """


            ## Fixes

            ## Questions
            """;
    private static final Pattern           ID       = Pattern.compile("F-[0-9a-f]{6}");
    private static final DateTimeFormatter MINUTE   = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * @param changedFiles the paths {@code git status} lists, the report left out
     * @param players      the player counts as they were asked for, e.g. {@code 3-7}
     * @param roundCap     the run's round cap, for the command that plays a game again
     * @param traces       the folder of the traces, relative to the report
     */
    public record Run(LocalDateTime started, Duration duration, String commit, int changedFiles, long firstSeed,
                      int games, String players, int roundCap, String traces) {
    }

    enum Status {
        OPEN, QUESTION, REGRESSED;

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }

    private FuzzReport() {
    }

    @SneakyThrows
    static void write(Path report, Run run, RunResults results) {
        val existing = Files.exists(report) ? Files.readString(report, StandardCharsets.UTF_8) : null;
        Files.writeString(report, render(existing, run, results), StandardCharsets.UTF_8);
    }

    static String render(String existing, Run run, RunResults results) {
        val text = existing == null ? HEADER + BEGIN + "\n" + END + SECTIONS : existing.replace("\r\n", "\n");
        val begin = text.indexOf(BEGIN);
        val end = text.indexOf(END);
        final String head;
        final String oldBlock;
        final String tail;
        if (begin < 0 || end < begin) {
            head = text.stripTrailing() + "\n\n";
            oldBlock = "";
            tail = SECTIONS;
        } else {
            head = text.substring(0, begin);
            oldBlock = text.substring(begin + BEGIN.length(), end);
            tail = text.substring(end + END.length());
        }
        val handWritten = head + tail;
        val statuses = new Statuses(ids(section(handWritten, "Fixes")), ids(section(handWritten, "Questions")));
        return head + BEGIN + "\n" + block(run, results, statuses, historyRows(oldBlock)) + END + tail;
    }

    record Statuses(Set<String> fixed, Set<String> questions) {

        Status of(String id) {
            if (fixed.contains(id)) {
                return Status.REGRESSED;
            }
            return questions.contains(id) ? Status.QUESTION : Status.OPEN;
        }
    }

    private static String block(Run run, RunResults results, Statuses statuses, List<String> history) {
        val games = results.games();
        val counts = games.stream().map(PlayoutResult::players).distinct().sorted().toList();
        val out = new StringBuilder("\n## Last run\n\n");
        out.append(MINUTE.format(run.started())).append(", `").append(run.commit()).append("` (")
                .append(plural(run.changedFiles(), "changed file")).append("), ")
                .append(run.games() == 1 ? "seed " + run.firstSeed() : "seeds " + run.firstSeed() + "-"
                        + (run.firstSeed() + run.games() - 1))
                .append(", ").append(run.players()).append(" players, ").append(duration(run.duration())).append(".\n\n");

        out.append("| Players | Games | Finished | Failed | Humans won | Cylons won | Rounds (median / max) |\n");
        out.append("|---|---|---|---|---|---|---|\n");
        for (val count : counts) {
            out.append(gamesRow(String.valueOf(count), games.stream().filter(g -> g.players() == count).toList()));
        }
        out.append(gamesRow("All", games));

        out.append("\n### Endings\n\n| Ending |");
        counts.forEach(count -> out.append(' ').append(count).append(" |"));
        out.append("\n|---|").append("---|".repeat(counts.size())).append('\n');
        for (val ending : Ending.values()) {
            out.append("| ").append(ending.label).append(" |");
            for (val count : counts) {
                out.append(' ').append(games.stream()
                        .filter(g -> g.players() == count && g.ending().orElse(null) == ending)
                        .count()).append(" |");
            }
            out.append('\n');
        }

        val findings = sortedFindings(results, games);
        out.append("\n### Findings\n\n");
        if (findings.isEmpty()) {
            out.append("None.\n");
        } else {
            out.append("| ID | Kind | What | Games | Example | Status |\n|---|---|---|---|---|---|\n");
            for (val found : findings) {
                val example = results.example(found.finding().id());
                out.append("| ").append(found.finding().id()).append(" | ").append(found.finding().kind())
                        .append(" | ").append(cell(found.finding().summary())).append(" | ").append(found.games())
                        .append(" | ").append(example.players()).append("p seed ").append(example.seed())
                        .append(" | ").append(statuses.of(found.finding().id())).append(" |\n");
            }
            out.append('\n');
            for (val found : findings) {
                val finding = found.finding();
                val example = results.example(finding.id());
                out.append("- **").append(finding.id()).append("**: `").append(command(example, run.roundCap()))
                        .append("`, trace `").append(run.traces()).append('/').append(traceName(finding, example))
                        .append("`.");
                if (finding.pause() != null) {
                    out.append(' ').append(finding.pause().where()).append('.');
                }
                out.append('\n');
            }
        }

        out.append("\n### Watch\n\n");
        val watch = watch(games, counts);
        out.append(watch.isEmpty() ? "Nothing to watch.\n" : watch.stream().map(line -> "- " + line + "\n")
                .collect(Collectors.joining()));

        val neverRan = new TreeSet<>(ProductionEvents.types());
        neverRan.removeAll(results.eventTypes());
        out.append("\n<details><summary>Event types that never ran (").append(neverRan.size())
                .append(")</summary>\n\n")
                .append(neverRan.stream().map(name -> "`" + name + "`").collect(Collectors.joining(", ")))
                .append("\n\n</details>\n");

        val open = findings.stream().filter(found -> statuses.of(found.finding().id()) != Status.QUESTION).count();
        out.append("\n### History\n\n| Run | Commit | Games | Failed | Open findings |\n|---|---|---|---|---|\n");
        out.append("| ").append(MINUTE.format(run.started())).append(" | `").append(run.commit()).append("` | ")
                .append(games.size()).append(" | ").append(results.failed()).append(" | ").append(open).append(" |\n");
        history.forEach(row -> out.append(row).append('\n'));
        return out.append('\n').toString();
    }

    record Found(Finding finding, int games) {
    }

    // The ones hit by the most games first, then by the example's player count and seed: the fix loop's order.
    static List<Found> sortedFindings(RunResults results, List<PlayoutResult> games) {
        val hits = new HashMap<String, Integer>();
        for (val game : games) {
            game.findings().forEach(finding -> hits.merge(finding.id(), 1, Integer::sum));
        }
        return results.findings().stream()
                .map(finding -> new Found(finding, hits.getOrDefault(finding.id(), 0)))
                .sorted(Comparator.comparingInt(Found::games).reversed()
                        .thenComparing(found -> results.example(found.finding().id()), RunResults.BY_GAME)
                        .thenComparing(found -> found.finding().id()))
                .toList();
    }

    private static String gamesRow(String label, List<PlayoutResult> games) {
        val finished = games.stream().filter(PlayoutResult::finished).toList();
        val rounds = finished.stream().mapToInt(PlayoutResult::rounds).sorted().toArray();
        val roundsCell = rounds.length == 0 ? "-" : rounds[(rounds.length - 1) / 2] + " / " + rounds[rounds.length - 1];
        return "| " + label + " | " + games.size() + " | " + finished.size() + " | "
                + games.stream().filter(PlayoutResult::failed).count() + " | "
                + finished.stream().filter(g -> g.winner().orElse(null) == Team.HUMANS).count() + " | "
                + finished.stream().filter(g -> g.winner().orElse(null) == Team.CYLONS).count() + " | "
                + roundsCell + " |\n";
    }

    // A team that never wins, or an ending that never happens: a smoke test for broken distributions, not balance.
    private static List<String> watch(List<PlayoutResult> games, List<Integer> counts) {
        val result = new ArrayList<String>();
        for (val count : counts) {
            val ofCount = games.stream().filter(g -> g.players() == count).toList();
            for (val team : Team.values()) {
                if (ofCount.stream().noneMatch(g -> g.finished() && g.winner().orElse(null) == team)) {
                    result.add(count + " players: the " + (team == Team.HUMANS ? "humans" : "Cylons") + " never won in "
                            + plural(ofCount.size(), "game") + ".");
                }
            }
            if (ofCount.stream().noneMatch(g -> g.ending().filter(FuzzReport::atKobol).isPresent())) {
                result.add(count + " players: no Kobol ending in " + plural(ofCount.size(), "game") + ".");
            }
        }
        for (val ending : Ending.values()) {
            if (!atKobol(ending) && games.stream().noneMatch(g -> g.ending().orElse(null) == ending)) {
                result.add("No \"" + ending.label + "\" ending in " + plural(games.size(), "game") + ".");
            }
        }
        return result;
    }

    private static boolean atKobol(Ending ending) {
        return ending == Ending.KOBOL_HUMANS || ending == Ending.KOBOL_CYLONS;
    }

    static String command(PlayoutResult game, int roundCap) {
        return "./gradlew :core:fuzz -Pplayers=" + game.players() + " -Pseed=" + game.seed()
                + (roundCap == Playout.Options.ROUND_CAP ? "" : " -Prounds=" + roundCap);
    }

    static String traceName(Finding finding, PlayoutResult game) {
        return finding.id() + "-" + game.players() + "p-" + game.seed() + ".txt";
    }

    static String duration(Duration duration) {
        val seconds = duration.toSeconds();
        if (seconds < 60) {
            return seconds + "s";
        }
        if (seconds < 3600) {
            return seconds / 60 + "m " + seconds % 60 + "s";
        }
        return seconds / 3600 + "h " + seconds % 3600 / 60 + "m";
    }

    private static String plural(long count, String noun) {
        return count + " " + noun + (count == 1 ? "" : "s");
    }

    private static String cell(String text) {
        return text.replace("|", "\\|").replace("\n", " ");
    }

    // The lines under a "## name" heading, up to the next heading of that level.
    static String section(String text, String name) {
        val result = new StringBuilder();
        var inside = false;
        for (val line : text.split("\n", -1)) {
            if (line.startsWith("## ")) {
                inside = line.substring(3).strip().equals(name);
                continue;
            }
            if (inside) {
                result.append(line).append('\n');
            }
        }
        return result.toString();
    }

    private static Set<String> ids(String text) {
        val result = new TreeSet<String>();
        val matcher = ID.matcher(text);
        while (matcher.find()) {
            result.add(matcher.group());
        }
        return result;
    }

    // The rows of the history table, without its header.
    static List<String> historyRows(String block) {
        val lines = Arrays.asList(block.split("\n", -1));
        val start = lines.indexOf("### History");
        if (start < 0) {
            return List.of();
        }
        return lines.subList(start + 1, lines.size()).stream()
                .takeWhile(line -> line.isBlank() || line.startsWith("|"))
                .filter(line -> line.startsWith("|"))
                .filter(line -> !line.startsWith("| Run |") && !line.startsWith("|---"))
                .toList();
    }
}
