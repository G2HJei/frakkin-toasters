package xyz.zlatanov.frakkintoasters.event.skillcheck.result;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.board.LoseResourceEvent;

import static xyz.zlatanov.frakkintoasters.state.board.Resource.POPULATION;

class KeepTabsOnVisitorFailEventProcessorTest extends EventTestHarness<KeepTabsOnVisitorFailEvent> {

    @ParameterizedTest
    @ValueSource(ints = {1, 4})
    void shouldLosePopulationOnLowRoll(int roll) {
        nextRoll(roll);

        execute(new KeepTabsOnVisitorFailEvent());

        assertFollowup(new LoseResourceEvent(POPULATION, 2));
    }

    @ParameterizedTest
    @ValueSource(ints = {5, 8})
    void shouldDoNothingOnHighRoll(int roll) {
        nextRoll(roll);

        execute(new KeepTabsOnVisitorFailEvent());
    }
}
