package xyz.zlatanov.frakkintoasters.event.action;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.GameFact.ActingPlayerChanged;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResetActingPlayerEventProcessorTest extends EventTestHarness<ResetActingPlayerEvent> {

    @Test
    void theCurrentPlayerActsAgain() {
        ops.actingPlayer(2);

        execute(new ResetActingPlayerEvent());

        assertEquals(1, game.turn().actingPlayerNumber());
    }

    @Test
    void nothingToResetLogsNothing() {
        execute(new ResetActingPlayerEvent());

        assertTrue(journal.facts().stream().noneMatch(ActingPlayerChanged.class::isInstance));
    }
}
