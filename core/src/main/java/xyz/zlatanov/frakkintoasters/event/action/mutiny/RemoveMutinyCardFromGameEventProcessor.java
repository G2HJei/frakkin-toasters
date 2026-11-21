package xyz.zlatanov.frakkintoasters.event.action.mutiny;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

public class RemoveMutinyCardFromGameEventProcessor extends EventProcessor<RemoveMutinyCardFromGameEvent> {

    @Override
    protected boolean isValid() {
        return event.card() != null;
    }

    @Override
    public Followup process() {
        ops.removeMutinyCardFromGame(event.card());
        return Followup.NONE;
    }
}
