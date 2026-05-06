package xyz.zlatanov.frakkintoasters.event;

import lombok.val;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.ship.Basestar;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.state.damage.BasestarDamage.DISABLED_WEAPONS;

public record ActivateCylonBasestarEvent(int basestarShipId) implements Event {

    @Override
    public Followup apply(Game game) {
        val basestar = game.boards().galactica().shipInSpace(basestarShipId, Basestar.class);
        val weaponsDisabled = basestar.damage().contains(DISABLED_WEAPONS);
        if (!weaponsDisabled && game.die().roll() > 3) {
            return one(new DamageGalacticaEvent(), new DamagePegasusEvent());
        } else {
            return NONE;
        }
    }
}
