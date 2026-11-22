package xyz.zlatanov.frakkintoasters.fuzz;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.engine.ScriptedTurns;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.fuzz.Finding.Kind.FUZZER;
import static xyz.zlatanov.frakkintoasters.fuzz.Finding.Kind.RESTORE;

// Two rounds from the scripted start. What the engine does wrong doesn't fail this test (the fuzzer's run reports it),
// so an open finding never turns the build red: only the fuzzer's own findings and a restore that differs do.
class PlayoutTest {

    private static final Playout.Options TWO_ROUNDS = Playout.Options.defaults()
            .withRoundCap(2)
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

    @Test
    void aSeedChangesTheGame() {
        val one = Playout.play(3, 1, TWO_ROUNDS);
        val other = Playout.play(3, 2, TWO_ROUNDS);

        assertTrue(!one.inputs().equals(other.inputs()));
    }
}
