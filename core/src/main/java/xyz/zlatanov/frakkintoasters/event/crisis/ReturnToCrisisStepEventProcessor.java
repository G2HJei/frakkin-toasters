package xyz.zlatanov.frakkintoasters.event.crisis;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

public class ReturnToCrisisStepEventProcessor extends EventProcessor<ReturnToCrisisStepEvent> {

    // The Activate Cylon Ships step does it, once the icons of this card have activated.
    @Override
    public Followup process() {
        ops.returnToCrisisStep(true);
        return Followup.NONE;
    }
}
