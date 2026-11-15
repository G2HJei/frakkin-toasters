package xyz.zlatanov.frakkintoasters.event.skillcheck.result;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.board.LoseResourceEvent;

import static xyz.zlatanov.frakkintoasters.state.board.Resource.POPULATION;

class KeepTabsOnVisitorFailResultEventProcessorTest extends EventTestHarness<KeepTabsOnVisitorFailResultEvent> {

    @ParameterizedTest
    @ValueSource(ints = {1, 4})
    void shouldLosePopulationOnLowRoll(int roll) {
        rolled(roll);

        execute(new KeepTabsOnVisitorFailResultEvent());

        assertFollowup(new LoseResourceEvent(POPULATION, 2));
    }

    @ParameterizedTest
    @ValueSource(ints = {5, 8})
    void shouldDoNothingOnHighRoll(int roll) {
        rolled(roll);

        execute(new KeepTabsOnVisitorFailResultEvent());
    }
}
