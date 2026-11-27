package xyz.zlatanov.frakkintoasters.fuzz;

import lombok.val;

/**
 * They hold nothing that changes between two runs of the same seed.
 */
final class Traces {

    private Traces() {
    }

    static String ofFinding(PlayoutResult game, Finding finding, int roundCap) {
        val out = new StringBuilder();
        out.append(finding.id()).append(' ').append(finding.kind()).append(": ").append(finding.summary()).append('\n');
        out.append("Game: ").append(game.players()).append(" players, seed ").append(game.seed()).append(" (")
                .append(FuzzReport.command(game, roundCap)).append(")\n");
        val pause = finding.pause();
        if (pause != null) {
            out.append("Where: ").append(pause.where()).append('\n');
        }
        if (finding.event() != null) {
            out.append("Event: ").append(finding.event()).append('\n');
        }
        if (pause != null) {
            out.append("\nPending input:\n").append(pause.pending()).append('\n');
        }
        if (!finding.detail().isEmpty()) {
            out.append("\nDetail:\n").append(finding.detail().stripTrailing()).append('\n');
        }
        if (pause != null) {
            out.append("\nInputs before it, oldest first (").append(pause.inputs().size()).append("):\n");
            pause.inputs().forEach(line -> out.append(line).append('\n'));
        }
        return out.toString();
    }

    static String ofGame(PlayoutResult game, int roundCap) {
        val out = new StringBuilder();
        out.append(game.players()).append(" players, seed ").append(game.seed()).append(" (")
                .append(FuzzReport.command(game, roundCap)).append("): ").append(outcome(game)).append(", ")
                .append(game.rounds()).append(" rounds, ").append(game.inputCount()).append(" inputs\n");
        out.append("\nFindings:\n");
        if (game.findings().isEmpty()) {
            out.append("none\n");
        }
        for (val finding : game.findings()) {
            out.append("- ").append(finding.id()).append(' ').append(finding.kind()).append(": ")
                    .append(finding.summary());
            if (finding.pause() != null) {
                out.append(". ").append(finding.pause().where());
            }
            out.append('\n');
        }
        out.append("\nInputs (").append(game.inputs().size()).append("):\n");
        game.inputs().forEach(line -> out.append(line).append('\n'));
        return out.toString();
    }

    static String outcome(PlayoutResult game) {
        if (game.failed()) {
            return "stopped by " + game.stoppedBy().id() + " " + game.stoppedBy().kind();
        }
        val ending = game.ending().map(e -> " (" + e.label + ")").orElse("");
        return "the " + game.winner().map(team -> team.name().toLowerCase()).orElse("nobody") + " won" + ending;
    }
}
