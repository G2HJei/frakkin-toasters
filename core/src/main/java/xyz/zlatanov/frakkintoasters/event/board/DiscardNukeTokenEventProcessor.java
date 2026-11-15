package xyz.zlatanov.frakkintoasters.event.board;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

public class DiscardNukeTokenEventProcessor extends EventProcessor<DiscardNukeTokenEvent> {

    @Override
    protected boolean isValid() {
        return game.nukes() > 0;
    }

    @Override
    public Followup process() {
        ops.discardNuke();
        return Followup.NONE;
    }
}
