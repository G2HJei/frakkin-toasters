package xyz.zlatanov.frakkintoasters.event.ship;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

public class DamageGalacticaEventProcessor extends EventProcessor<DamageGalacticaEvent> {

    @Override
    public Followup process() {
        ops.galactica().damage();
        return Followup.NONE;
    }
}
