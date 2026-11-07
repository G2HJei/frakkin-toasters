package xyz.zlatanov.frakkintoasters.event.board;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.journal.fact.BoardFact.ResourceDecreased;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.board.Resource.FOOD;
import static xyz.zlatanov.frakkintoasters.state.board.Resource.MORALE;

class LoseResourceEventProcessorTest extends EventTestHarness<LoseResourceEvent> {

    @Test
    void shouldLoseResource() {
        execute(new LoseResourceEvent(FOOD, 2));

        assertEquals(6, galacticaBoard.food());
        assertEquals(List.of(new ResourceDecreased(FOOD, 2)), game.journal().facts());
    }

    @Test
    void shouldNotGoBelowZero() {
        execute(new LoseResourceEvent(MORALE, 100));

        assertEquals(0, galacticaBoard.morale());
    }
}
