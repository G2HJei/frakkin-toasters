package xyz.zlatanov.frakkintoasters.event.jump;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

public class ResetJumpPreparationEventProcessor extends EventProcessor<ResetJumpPreparationEvent> {

    @Override
    public Followup process() {
        ops.galactica().resetJumpPreparation();
        return Followup.NONE;
    }
}
