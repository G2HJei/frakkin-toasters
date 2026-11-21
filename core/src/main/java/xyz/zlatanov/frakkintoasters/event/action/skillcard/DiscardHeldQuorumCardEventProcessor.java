package xyz.zlatanov.frakkintoasters.event.action.skillcard;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

public class DiscardHeldQuorumCardEventProcessor extends EventProcessor<DiscardHeldQuorumCardEvent> {

    @Override
    protected boolean isValid() {
        return event.card() != null && player.quorumCards().cards().contains(event.card());
    }

    @Override
    public Followup process() {
        ops.player(player.number()).discardHeldQuorumCard(event.card());
        return Followup.NONE;
    }
}
