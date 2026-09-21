package xyz.zlatanov.frakkintoasters.event.location;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.event.ActivateViperEvent;
import xyz.zlatanov.frakkintoasters.event.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;

class CommandEventProcessorTest extends EventTestHarness<CommandEvent> {

    @Test
    void shouldFollowupWithActivateViperDecisions() {
        execute(new CommandEvent(1));
        assertFollowup(
                all(
                        new PlayerDecisionEvent<>(1, ActivateViperEvent.class),
                        new PlayerDecisionEvent<>(1, ActivateViperEvent.class)));
    }
}