package xyz.zlatanov.frakkintoasters.event.deck;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

public class ReplaceDestinyDeckEventProcessor extends EventProcessor<ReplaceDestinyDeckEvent> {

    @Override
    protected boolean isValid() {
        return event.color() != null && event.count() > 0;
    }

    @Override
    public Followup process() {
        ops.skillCheck().replaceDestinyDeck(event.color(), event.count());
        return Followup.NONE;
    }
}
