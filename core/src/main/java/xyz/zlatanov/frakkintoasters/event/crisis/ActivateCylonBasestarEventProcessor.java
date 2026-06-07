package xyz.zlatanov.frakkintoasters.event.crisis;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.DamageGalacticaEvent;
import xyz.zlatanov.frakkintoasters.event.DamagePegasusEvent;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.ship.Basestar;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.state.damage.BasestarDamage.DISABLED_WEAPONS;

public class ActivateCylonBasestarEventProcessor extends EventProcessor<ActivateCylonBasestarEvent> {
    @Override
    public Followup process() {
        val basestar = galacticaBoard.shipInSpace(event.basestarShipId(), Basestar.class);
        val weaponsDisabled = basestar.damage().contains(DISABLED_WEAPONS);
        if (!weaponsDisabled && rollDie() > 3) {
            return Followup.one(new DamageGalacticaEvent(), new DamagePegasusEvent());
        } else {
            return NONE;
        }
    }
}
