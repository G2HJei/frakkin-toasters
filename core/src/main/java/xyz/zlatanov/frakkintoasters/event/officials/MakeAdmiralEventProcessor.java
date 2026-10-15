package xyz.zlatanov.frakkintoasters.event.officials;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

public class MakeAdmiralEventProcessor extends EventProcessor<MakeAdmiralEvent> {

    @Override
    public Followup process() {
        ops.titles().assignAdmiral(event.character());
        return Followup.NONE;
    }
}
