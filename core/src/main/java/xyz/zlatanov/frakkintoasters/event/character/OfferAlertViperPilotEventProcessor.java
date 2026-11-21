package xyz.zlatanov.frakkintoasters.event.character;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

public class OfferAlertViperPilotEventProcessor extends EventProcessor<OfferAlertViperPilotEvent> {

    @Override
    protected boolean isValid() {
        return event.viperIds() != null;
    }

    @Override
    public Followup process() {
        return AlertViperPilotEventProcessor.offer(game, event.viperIds());
    }
}
