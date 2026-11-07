package xyz.zlatanov.frakkintoasters.event.board;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.journal.fact.BoardFact.ResourceIncreased;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.board.Resource.MORALE;
import static xyz.zlatanov.frakkintoasters.state.board.Resource.POPULATION;

class GainResourceEventProcessorTest extends EventTestHarness<GainResourceEvent> {

    @Test
    void shouldGainResource() {
        execute(new GainResourceEvent(POPULATION, 1));

        assertEquals(13, galacticaBoard.population());
        assertEquals(List.of(new ResourceIncreased(POPULATION, 1)), game.journal().facts());
    }

    @Test
    void shouldNotGoAboveTheMaximum() {
        execute(new GainResourceEvent(MORALE, 100));

        assertEquals(15, galacticaBoard.morale());
    }
}
