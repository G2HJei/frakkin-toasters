package xyz.zlatanov.frakkintoasters.engine;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

public class StackCrisisCardEventProcessor extends EventProcessor<StackCrisisCardEvent> {

    @Override
    public Followup process() {
        ops.player(1).placeCrisisCardOnTop(event.card());
        return Followup.NONE;
    }
}
