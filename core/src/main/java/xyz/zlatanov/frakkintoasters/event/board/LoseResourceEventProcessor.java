package xyz.zlatanov.frakkintoasters.event.board;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

public class LoseResourceEventProcessor extends EventProcessor<LoseResourceEvent> {

    @Override
    public Followup process() {
        ops.galactica().decreaseResource(event.resource(), event.amount());
        return Followup.NONE;
    }
}
