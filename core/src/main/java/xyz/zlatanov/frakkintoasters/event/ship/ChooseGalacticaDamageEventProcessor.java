package xyz.zlatanov.frakkintoasters.event.ship;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

public class ChooseGalacticaDamageEventProcessor extends EventProcessor<ChooseGalacticaDamageEvent> {

    @Override
    protected boolean isValid() {
        return event.resolved() != null && event.returned() != null;
    }

    @Override
    public Followup process() {
        ops.galactica().resolveDamage(event.resolved());
        ops.galactica().returnDamage(event.returned());
        return Followup.NONE;
    }
}
