package xyz.zlatanov.frakkintoasters.event.endgame;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.GameFact.GameEnded;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.Team.CYLONS;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;

class CylonsWinEventProcessorTest extends EventTestHarness<CylonsWinEvent> {

    @Test
    void theCylonsWin() {
        player(1).character(KARA_STARBUCK_THRACE);
        player(2).character(WILLIAM_ADAMA);
        player(3).character(LAURA_ROSLIN);

        execute(new CylonsWinEvent());

        assertEquals(Optional.of(CYLONS), game.outcome());
        assertEquals(List.of(new GameEnded(CYLONS, List.of())), journal.facts());
    }
}
