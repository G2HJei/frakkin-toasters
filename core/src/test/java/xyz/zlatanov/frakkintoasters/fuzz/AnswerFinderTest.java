package xyz.zlatanov.frakkintoasters.fuzz;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.engine.GameEngine;
import xyz.zlatanov.frakkintoasters.engine.PendingInput.AwaitingDecision;
import xyz.zlatanov.frakkintoasters.engine.ScriptedTurns;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class AnswerFinderTest {

    // At every decision of the scripted turns, the finder finds an answer, and the game takes it.
    @Test
    void everyDecisionOfTheScriptedTurnsHasAnAnswerTheGameTakes() {
        val engine = ScriptedTurns.start();
        val findings = new ArrayList<Finding>();
        var decisions = 0;
        for (val input : ScriptedTurns.inputs()) {
            if (engine.pendingInput().orElseThrow() instanceof AwaitingDecision pending) {
                val decision = pending.decision();
                val answer = AnswerFinder.find(engine.view(), decision, new Random(decisions), findings::add);

                assertTrue(answer.isPresent(), "no answer to " + decision);
                val copy = GameEngine.restore(engine.journal(), ScriptedTurns.SEED);
                assertDoesNotThrow(() -> copy.submit(decision.playerNumber(), answer.get()), answer.get().toString());
                decisions++;
            }
            input.apply(engine.view()).applyTo(engine);
        }
        assertTrue(decisions > 10);
        assertEquals(List.of(), findings.stream().filter(finding -> finding.kind() == Finding.Kind.FUZZER).toList());
    }
}
