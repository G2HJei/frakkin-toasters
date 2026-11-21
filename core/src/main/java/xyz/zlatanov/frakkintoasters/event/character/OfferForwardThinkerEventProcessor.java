package xyz.zlatanov.frakkintoasters.event.character;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

public class OfferForwardThinkerEventProcessor extends EventProcessor<OfferForwardThinkerEvent> {

    @Override
    public Followup process() {
        // What the other player did may have moved him, e.g. to the "Brig".
        return ForwardThinkerEventProcessor.offer(game, player);
    }
}
