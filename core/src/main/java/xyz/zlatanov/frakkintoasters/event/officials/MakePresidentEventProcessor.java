package xyz.zlatanov.frakkintoasters.event.officials;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

public class MakePresidentEventProcessor extends EventProcessor<MakePresidentEvent> {
    @Override
    public Followup process() {
        game.president(event.character());
        return Followup.NONE;
    }
}
