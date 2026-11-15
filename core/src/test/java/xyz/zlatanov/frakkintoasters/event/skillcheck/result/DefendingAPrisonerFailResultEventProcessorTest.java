package xyz.zlatanov.frakkintoasters.event.skillcheck.result;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.player.ExecuteEvent;

import static xyz.zlatanov.frakkintoasters.state.board.Location.CAPRICA;
import static xyz.zlatanov.frakkintoasters.state.board.Location.COMMAND;
import static xyz.zlatanov.frakkintoasters.state.character.Character.FELIX_GAETA;

class DefendingAPrisonerFailResultEventProcessorTest extends EventTestHarness<DefendingAPrisonerFailResultEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(FELIX_GAETA);
        moveTo(COMMAND, FELIX_GAETA);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 4})
    void shouldExecuteCurrentPlayerOnLowRoll(int roll) {
        rolled(roll);

        execute(new DefendingAPrisonerFailResultEvent());

        assertFollowup(new ExecuteEvent(1));
    }

    @ParameterizedTest
    @ValueSource(ints = {5, 8})
    void shouldDoNothingOnHighRoll(int roll) {
        rolled(roll);

        execute(new DefendingAPrisonerFailResultEvent());
    }

    // A revealed Cylon resolving the card at "Caprica" ignores the execution (rule 6.1.2.1). ExecuteEvent would reject
    // him, so it can't be queued.
    @Test
    void shouldNotExecuteARevealedCylon() {
        moveTo(CAPRICA, FELIX_GAETA);
        revealCylon();
        rolled(1);

        execute(new DefendingAPrisonerFailResultEvent());
    }
}
