package xyz.zlatanov.frakkintoasters.event.skillcheck.result;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.player.ExecuteEvent;

class DefendingAPrisonerFailEventProcessorTest extends EventTestHarness<DefendingAPrisonerFailEvent> {

    @BeforeEach
    void setUp() {
        game.turn().currentPlayerNumber(2);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 4})
    void shouldExecuteCurrentPlayerOnLowRoll(int roll) {
        nextRoll(roll);

        execute(new DefendingAPrisonerFailEvent());

        assertFollowup(new ExecuteEvent(2));
    }

    @ParameterizedTest
    @ValueSource(ints = {5, 8})
    void shouldDoNothingOnHighRoll(int roll) {
        nextRoll(roll);

        execute(new DefendingAPrisonerFailEvent());
    }
}
