package xyz.zlatanov.frakkintoasters.event.board;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

public class GainResourceEventProcessor extends EventProcessor<GainResourceEvent> {

    @Override
    public Followup process() {
        ops.galactica().increaseResource(event.resource(), event.amount());
        return Followup.NONE;
    }
}
