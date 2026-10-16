package xyz.zlatanov.frakkintoasters.event.deck;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

//todo maybe there is no need for a separate event as it is part of setup and then is recreated once depleted anyway
public class CreateDestinyDeckEventProcessor extends EventProcessor<CreateDestinyDeckEvent> {
    @Override
    public Followup process() {
        ops.skillCheck().createDestinyDeck();
        return Followup.NONE;
    }
}
