package xyz.zlatanov.frakkintoasters.event.location.galactica;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.ActivateViperEvent;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.state.board.Location.COMMAND;
import static xyz.zlatanov.frakkintoasters.state.character.Character.SAUL_TIGH;

class CommandEventProcessorTest extends EventTestHarness<CommandEvent> {

    @BeforeEach
    void setUpAtLocation() {
        player(1).character(SAUL_TIGH);
        moveTo(COMMAND, SAUL_TIGH);
    }

    @Test
    void shouldFollowupWithUpTo2ActivateViperDecisions() {
        execute(new CommandEvent(1));
        assertFollowup(
                all(
                        one(new PlayerDecisionEvent<>(1, ActivateViperEvent.class), new NoOpEvent(1)),
                        one(new PlayerDecisionEvent<>(1, ActivateViperEvent.class), new NoOpEvent(1))));
    }
}
