package xyz.zlatanov.frakkintoasters.fuzz;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.engine.ScriptedTurns;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.fuzz.Finding.Kind.FUZZER;
import static xyz.zlatanov.frakkintoasters.fuzz.Finding.Kind.RESTORE;

// Two rounds from the scripted start. What the engine does wrong doesn't fail this test (the fuzzer's run reports it),
// so an open finding never turns the build red: only the fuzzer's own findings and a restore that differs do.
class PlayoutTest {

    // Without the probes: an engine bug they hit would end the game early (ProbeTest covers them).
    private static final Playout.Options TWO_ROUNDS = Playout.Options.defaults()
            .withRoundCap(2)
            .withProbeChance(0)
            .withFirstEvent(ScriptedTurns.firstEvent());

    @Test
    void theSameSeedPlaysTheSameGame() {
        val first = Playout.play(3, ScriptedTurns.SEED, TWO_ROUNDS);
        val second = Playout.play(3, ScriptedTurns.SEED, TWO_ROUNDS);

        assertTrue(first.inputCount() > 50, "played " + first.inputCount() + " inputs");
        assertEquals(first.inputs(), second.inputs());
        // the details hold the stacks, which end in this test's lines
        assertEquals(first.findings().stream().map(Finding::id).toList(),
                second.findings().stream().map(Finding::id).toList());
    }

    @Test
    void theFuzzerItselfFindsNothingAndTheRestoredGamesMatch() {
        val result = Playout.play(3, ScriptedTurns.SEED, TWO_ROUNDS);

        assertEquals(List.of(), result.findings().stream()
                .filter(finding -> Set.of(FUZZER, RESTORE).contains(finding.kind()))
                .map(Finding::summary)
                .toList());
    }

    // A probe at every decision: whatever the engine does with them, the same seed probes the same way.
    @Test
    void theSameSeedProbesTheSameWay() {
        val probing = TWO_ROUNDS.withProbeChance(1);

        val first = Playout.play(3, ScriptedTurns.SEED, probing);
        val second = Playout.play(3, ScriptedTurns.SEED, probing);

        assertTrue(first.inputs().stream().anyMatch(input -> input.contains(": probe ")));
        assertEquals(first.inputs(), second.inputs());
        assertEquals(first.findings().stream().map(Finding::id).toList(),
                second.findings().stream().map(Finding::id).toList());
        assertEquals(List.of(), first.findings().stream()
                .filter(finding -> finding.kind() == FUZZER)
                .map(Finding::summary)
                .toList());
    }

    // The repro of a finding holds the journal at its pause, and keeping it changes nothing in the game.
    @Test
    void theReprosChangeNothingInTheGame() {
        val plain = Playout.play(3, ScriptedTurns.SEED, TWO_ROUNDS.withProbeChance(1));
        val withRepros = Playout.play(3, ScriptedTurns.SEED, TWO_ROUNDS.withProbeChance(1).withRepro());

        assertEquals(plain.inputs(), withRepros.inputs());
        for (val finding : withRepros.findings()) {
            assertNotNull(finding.pause().repro(), finding.summary());
            assertDoesNotThrow(finding.pause().repro()::restore, finding.summary());
        }
    }

    @Test
    void aSeedChangesTheGame() {
        val one = Playout.play(3, 1, TWO_ROUNDS);
        val other = Playout.play(3, 2, TWO_ROUNDS);

        assertTrue(!one.inputs().equals(other.inputs()));
    }
}
