package xyz.zlatanov.frakkintoasters.event.action.quorum;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

public class DiscardPlayedQuorumCardEventProcessor extends EventProcessor<DiscardPlayedQuorumCardEvent> {

    @Override
    protected boolean isValid() {
        return event.card() != null;
    }

    @Override
    public Followup process() {
        ops.titles().discardPlayedQuorumCard(event.card());
        return Followup.NONE;
    }
}
