package xyz.zlatanov.frakkintoasters.event.endgame;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.GameFact.GameEnded;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.Team.CYLONS;

class CylonsWinEventProcessorTest extends EventTestHarness<CylonsWinEvent> {

    @Test
    void theCylonsWin() {
        execute(new CylonsWinEvent());

        assertEquals(Optional.of(CYLONS), game.outcome());
        assertEquals(List.of(new GameEnded(CYLONS)), journal.facts());
    }
}
