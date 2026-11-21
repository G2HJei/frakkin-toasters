package xyz.zlatanov.frakkintoasters.event.character;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

public class OfferAdaptableEventProcessor extends EventProcessor<OfferAdaptableEvent> {

    @Override
    public Followup process() {
        // What the card did may have changed who can use it, e.g. "Execute Prisoner".
        return AdaptableEventProcessor.offer(game);
    }
}
