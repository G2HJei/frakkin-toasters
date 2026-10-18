package xyz.zlatanov.frakkintoasters.event.skillcheck.ability;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

public class IronWillEventProcessor extends EventProcessor<IronWillEvent> {

    @Override
    public Followup process() {
        ops.galactica().decreaseMorale(1);
        return Followup.NONE;
    }
}
