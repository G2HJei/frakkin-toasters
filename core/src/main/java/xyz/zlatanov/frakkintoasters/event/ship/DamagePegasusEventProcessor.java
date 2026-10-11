package xyz.zlatanov.frakkintoasters.event.ship;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

public class DamagePegasusEventProcessor extends EventProcessor<DamagePegasusEvent> {
    @Override
    public Followup process() {
        ops.pegasus().damage();
        return Followup.NONE;
    }
}
