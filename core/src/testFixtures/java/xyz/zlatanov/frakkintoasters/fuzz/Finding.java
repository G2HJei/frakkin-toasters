package xyz.zlatanov.frakkintoasters.fuzz;

import lombok.SneakyThrows;
import lombok.val;
import xyz.zlatanov.frakkintoasters.event.Event;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Something a random game showed that is wrong. The signature leaves out line numbers and the values of the game, so
 * the same bug has the same ID in every game and after its code moves.
 *
 * @param signature what tells this bug from another: the kind, the exception and where it was thrown, the event
 * @param summary   one line for the report
 * @param detail    the message and the stack, for the trace
 * @param event     the event that was checked or run, when the finding is about one a client could send
 * @param pause     where the game first showed it, {@code null} until the playout notes it
 */
public record Finding(Kind kind, String signature, String summary, String detail, Event event, Pause pause) {

    private static final String  PROJECT = "xyz.zlatanov.frakkintoasters.";
    private static final Pattern QUEUED  = Pattern.compile("The queued (\\w+)");

    public enum Kind {
        CRASH(true), // an accepted input rolled back, or threw an Error past the rollback
        INPUT_CRASH(false), // checking an input threw instead of rejecting it
        NO_ANSWER(true), // the asked player can't answer
        DEAD_OPTION(false), // an option of a choice leads to a decision without an answer
        RESTORE(true), // a restore threw, or gave another game
        NO_END(true), // the round cap, or the inputs cap within a turn
        BAD_ENDING(true), // a finished game that no ending explains
        REJECTED_BUT_CHANGED(true), // a rejected input changed the game
        FUZZER(false); // the fuzzer itself

        public final boolean fatal;

        Kind(boolean fatal) {
            this.fatal = fatal;
        }
    }

    /**
     * The pause a game first showed a finding at.
     *
     * @param where   the turn, the step and what was asked, for the report
     * @param pending the pending input in full
     * @param inputs  the inputs given before it, the last ones unless the whole trace was asked for
     * @param repro   the journal at the pause and the input that failed, when the playout keeps them
     */
    public record Pause(String where, String pending, List<String> inputs, Repro repro) {
    }

    public static Finding of(Kind kind, String signature, String summary) {
        return new Finding(kind, signature, summary, "", null, null);
    }

    /**
     * @param event the simple name of the event that was checked or run
     */
    public static Finding of(Kind kind, Throwable thrown, String event) {
        val queued = QUEUED.matcher(String.valueOf(thrown.getMessage()));
        if (queued.find()) {
            // The input only led to it: the event the game queued and then refused is the bug.
            val name = queued.group(1);
            return new Finding(kind, "queued " + name + " invalid",
                    "The queued `" + name + "` is invalid (after `" + event + "`)", describe(thrown), null, null);
        }
        val root = rootCause(thrown);
        val frame = frame(root);
        val signature = root.getClass().getSimpleName() + " in " + frame + " on " + event;
        val summary = "`" + root.getClass().getSimpleName() + "` in `" + frame + "` on `" + event + "`";
        return new Finding(kind, signature, summary, describe(thrown), null, null);
    }

    public String id() {
        return "F-" + HexFormat.of().formatHex(sha256(kind + " " + signature)).substring(0, 6);
    }

    public boolean fatal() {
        return kind.fatal;
    }

    public Finding withDetail(String moreDetail) {
        return new Finding(kind, signature, summary, detail.isEmpty() ? moreDetail : detail + "\n" + moreDetail, event,
                pause);
    }

    public Finding withEvent(Event checked) {
        return new Finding(kind, signature, summary, detail, checked, pause);
    }

    public Finding at(Pause where) {
        return new Finding(kind, signature, summary, detail, event, where);
    }

    // What a run keeps of a game that isn't the example of the finding: the inputs and the journal take memory.
    Finding withoutPause() {
        return new Finding(kind, signature, summary, detail, null, null);
    }

    static Throwable rootCause(Throwable thrown) {
        var root = thrown;
        while (root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }
        return root;
    }

    // The first frame in the rules, else the first in the project: a frame in the deck or the fact applier says less,
    // and the processors' base class and the engine say nothing.
    private static String frame(Throwable thrown) {
        val frames = Arrays.stream(thrown.getStackTrace())
                .filter(frame -> frame.getClassName().startsWith(PROJECT))
                .filter(frame -> !frame.getClassName().startsWith(PROJECT + "fuzz."))
                .filter(frame -> !frame.getClassName().equals(PROJECT + "EventProcessor"))
                .filter(frame -> !frame.getClassName().startsWith(PROJECT + "engine."))
                .toList();
        return frames.stream()
                .filter(frame -> frame.getClassName().startsWith(PROJECT + "event."))
                .findFirst()
                .or(() -> frames.stream().findFirst())
                .map(frame -> simpleName(frame.getClassName()) + "." + frame.getMethodName())
                .orElse("?");
    }

    private static String simpleName(String className) {
        val name = className.substring(className.lastIndexOf('.') + 1);
        val nested = name.indexOf('$');
        return nested < 0 ? name : name.substring(0, nested);
    }

    private static String describe(Throwable thrown) {
        val result = new StringBuilder();
        for (Throwable t = thrown; t != null; t = t.getCause() == t ? null : t.getCause()) {
            result.append(t == thrown ? "" : "Caused by: ").append(t).append('\n');
            result.append(Arrays.stream(t.getStackTrace())
                    .limit(25)
                    .map(frame -> "    at " + frame)
                    .collect(Collectors.joining("\n"))).append('\n');
        }
        return result.toString();
    }

    @SneakyThrows
    private static byte[] sha256(String text) {
        return MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8));
    }
}
