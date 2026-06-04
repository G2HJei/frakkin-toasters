package xyz.zlatanov.frakkintoasters.event;

import xyz.zlatanov.frakkintoasters.EventProcessor;

public class AdvancePursuitTrackEventProcessor extends EventProcessor<AdvancePursuitTrackEvent> {
    @Override
    public Followup process() {
        cylonFleetBoard.advancePursuit();
        return Followup.NONE;
    }
}
