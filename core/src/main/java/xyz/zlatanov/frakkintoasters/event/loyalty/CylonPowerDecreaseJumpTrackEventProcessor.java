package xyz.zlatanov.frakkintoasters.event.loyalty;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.board.DecreaseJumpPreparationEvent;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;

public class CylonPowerDecreaseJumpTrackEventProcessor extends EventProcessor<CylonPowerDecreaseJumpTrackEvent> {

    @Override
    public Followup process() {
        return all(new DecreaseJumpPreparationEvent(), new DecreaseJumpPreparationEvent());
    }
}
