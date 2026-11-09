package xyz.zlatanov.frakkintoasters.engine;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

import static xyz.zlatanov.frakkintoasters.state.board.Resource.FUEL;

public class FailingEventProcessor extends EventProcessor<FailingEvent> {

    @Override
    public Followup process() {
        ops.galactica().decreaseResource(FUEL, 1);
        throw new IllegalStateException("Frak");
    }
}
