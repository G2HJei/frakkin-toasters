package xyz.zlatanov.frakkintoasters.event.location.pegasus;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.ship.DamageBasestarEvent;
import xyz.zlatanov.frakkintoasters.event.ship.DamagePegasusEvent;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.location.pegasus.PegasusCicEventProcessor.isBasestarInSpace;

public class PegasusCicResultEventProcessor extends EventProcessor<PegasusCicResultEvent> {

    @Override
    protected boolean isValid() {
        return isBasestarInSpace(game, event.basestarId());
    }

    @Override
    public Followup process() {
        val roll = game.turn().dieRoll().result();
        val damageBasestar = new DamageBasestarEvent(event.basestarId());
        if (roll <= 3) {
            return single(new DamagePegasusEvent());
        } else if (roll <= 6) {
            return single(damageBasestar);
        } else {
            return all(damageBasestar, damageBasestar);
        }
    }
}
