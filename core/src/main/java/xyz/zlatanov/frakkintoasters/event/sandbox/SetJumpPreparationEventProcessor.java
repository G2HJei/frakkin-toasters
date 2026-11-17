package xyz.zlatanov.frakkintoasters.event.sandbox;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

public class SetJumpPreparationEventProcessor extends EventProcessor<SetJumpPreparationEvent> {

    @Override
    protected boolean isValid() {
        return event.position() != null;
    }

    @Override
    public Followup process() {
        ops.sandbox().setJumpPreparation(event.position());
        return Followup.NONE;
    }
}
