package xyz.zlatanov.frakkintoasters.event.action.character;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

public class OrganizedNextEventProcessor extends EventProcessor<OrganizedNextEvent> {

    @Override
    protected boolean isValid() {
        return event.used() != null;
    }

    @Override
    public Followup process() {
        return OrganizedEventProcessor.next(game, event.playerNumber(), event.used());
    }
}
