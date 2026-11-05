package xyz.zlatanov.frakkintoasters.operation;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.TestHarness;
import xyz.zlatanov.frakkintoasters.journal.fact.GameFact.*;
import xyz.zlatanov.frakkintoasters.journal.fact.TitleFact.*;
import xyz.zlatanov.frakkintoasters.state.GameStep;
import xyz.zlatanov.frakkintoasters.state.character.Character;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AbstractOpsRecordTest extends TestHarness {

    @Test
    void recordAppliesTheFactAndLogsIt() {
        val character = Character.values()[0];
        new AbstractOps(game) {
            void run() {
                record(new TurnStarted(7));
                record(new StepChanged(GameStep.SETUP));
                record(new CurrentPlayerChanged(2));
                record(new ActingPlayerChanged(3));
                record(new PresidentAssigned(character));
            }
        }.run();

        assertEquals(7, game.turn());
        assertEquals(2, game.currentPlayerNumber());
        assertEquals(3, game.actingPlayerNumber());
        assertEquals(character, game.president());
        assertEquals(List.of(new TurnStarted(7), new StepChanged(GameStep.SETUP), new CurrentPlayerChanged(2),
                new ActingPlayerChanged(3), new PresidentAssigned(character)), game.journal().facts());
    }
}
