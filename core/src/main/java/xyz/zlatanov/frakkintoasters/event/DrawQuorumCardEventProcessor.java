package xyz.zlatanov.frakkintoasters.event;

import xyz.zlatanov.frakkintoasters.EventProcessor;

public class DrawQuorumCardEventProcessor extends EventProcessor<DrawQuorumCardEvent> {

    // The hand limit of 10 Quorum Cards applies at the end of the turn (rule 8.8).
    @Override
    public Followup process() {
        ops.titles().drawQuorumCard();
        return Followup.NONE;
    }
}
