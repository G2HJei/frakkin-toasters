package xyz.zlatanov.frakkintoasters.event.ship;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;

public class DamageBattlestarEventProcessor extends EventProcessor<DamageBattlestarEvent> {

    @Override
    public Followup process() {
        // Decided when the hit lands, not when it was queued: an earlier hit may have destroyed Pegasus.
        return pegasusBoard.destroyed()
                ? single(new DamageGalacticaEvent())
                : one(new DamageGalacticaEvent(), new DamagePegasusEvent());
    }
}
