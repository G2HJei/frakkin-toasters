package xyz.zlatanov.frakkintoasters.event.deck;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

public class Discard1MutinyCardEventProcessor extends EventProcessor<Discard1MutinyCardEvent> {

    @Override
    public Followup process() {
        ops.player(event.playerNumber()).discard(event.cardToDiscard());
        return Followup.NONE;
    }
}
