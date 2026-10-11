package xyz.zlatanov.frakkintoasters.event.deck;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

public class CreateDestinyDeckEventProcessor extends EventProcessor<CreateDestinyDeckEvent> {
    @Override
    public Followup process() {
        ops.destinyDeck().create();
        return Followup.NONE;
    }
}
