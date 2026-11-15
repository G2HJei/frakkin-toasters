package xyz.zlatanov.frakkintoasters.engine;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

public class SetUpGalacticaEventProcessor extends EventProcessor<SetUpGalacticaEvent> {

    @Override
    public Followup process() {
        ops.galactica().setUp();
        return Followup.NONE;
    }
}
