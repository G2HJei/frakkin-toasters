package xyz.zlatanov.frakkintoasters.event.board;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

public class DecreaseJumpPreparationEventProcessor extends EventProcessor<DecreaseJumpPreparationEvent> {

    @Override
    public Followup process() {
        ops.galactica().decreaseJumpPreparation();
        return Followup.NONE;
    }
}
