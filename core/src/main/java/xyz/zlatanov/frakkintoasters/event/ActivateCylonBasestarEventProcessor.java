package xyz.zlatanov.frakkintoasters.event;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.state.ship.Basestar;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.state.damage.BasestarDamage.DISABLED_WEAPONS;

public class ActivateCylonBasestarEventProcessor extends EventProcessor<ActivateCylonBasestarEvent> {
    @Override
    public Followup process() {
        val basestar = game.boards().galactica().shipInSpace(event.basestarShipId(), Basestar.class);
        val weaponsDisabled = basestar.damage().contains(DISABLED_WEAPONS);
        if (!weaponsDisabled && game.die().roll() > 3) {
            return one(new DamageGalacticaEvent(), new DamagePegasusEvent());
        } else {
            return NONE;
        }
    }
}
