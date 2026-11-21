package xyz.zlatanov.frakkintoasters.event.character;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

public class OfferDutifulEventProcessor extends EventProcessor<OfferDutifulEvent> {

    @Override
    protected boolean isValid() {
        return event.location() != null;
    }

    @Override
    public Followup process() {
        return DutifulEventProcessor.offer(game, player, event.location());
    }
}
