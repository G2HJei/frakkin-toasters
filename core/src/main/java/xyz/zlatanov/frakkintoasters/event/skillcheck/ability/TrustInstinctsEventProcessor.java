package xyz.zlatanov.frakkintoasters.event.skillcheck.ability;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

public class TrustInstinctsEventProcessor extends EventProcessor<TrustInstinctsEvent> {

    @Override
    public Followup process() {
        ops.skillCheck().playFromDestinyDeck(2, true);
        return Followup.NONE;
    }
}
