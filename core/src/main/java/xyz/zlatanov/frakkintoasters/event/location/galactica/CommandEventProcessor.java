package xyz.zlatanov.frakkintoasters.event.location.galactica;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.ActivateViperEvent;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;

public class CommandEventProcessor extends EventProcessor<CommandEvent> {
    @Override
    public Followup process() {
        return Followup.all(
                new PlayerDecisionEvent<>(player.number(), ActivateViperEvent.class),
                new PlayerDecisionEvent<>(player.number(), ActivateViperEvent.class)
        );
    }
}
