package xyz.zlatanov.frakkintoasters.event;

import xyz.zlatanov.frakkintoasters.EventProcessor;

public class DamageHumanFighterEventProcessor extends EventProcessor<DamageHumanFighterEvent> {

    @Override
    public Followup process() {
        ops.ship(event.shipId()).damage();
        return Followup.NONE;
    }
}
