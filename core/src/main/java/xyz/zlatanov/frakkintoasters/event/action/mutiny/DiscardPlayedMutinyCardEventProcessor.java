package xyz.zlatanov.frakkintoasters.event.action.mutiny;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

public class DiscardPlayedMutinyCardEventProcessor extends EventProcessor<DiscardPlayedMutinyCardEvent> {

    @Override
    protected boolean isValid() {
        return event.card() != null;
    }

    @Override
    public Followup process() {
        ops.discardPlayedMutinyCard(event.card());
        return Followup.NONE;
    }
}
