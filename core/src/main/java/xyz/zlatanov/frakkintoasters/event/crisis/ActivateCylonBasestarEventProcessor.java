package xyz.zlatanov.frakkintoasters.event.crisis;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.AttackGalacticaEvent;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.view.BasestarView;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.damage.BasestarDamage.DISABLED_WEAPONS;

public class ActivateCylonBasestarEventProcessor extends EventProcessor<ActivateCylonBasestarEvent> {

    @Override
    public Followup process() {
        if (game.turn().cylonShipsHeld()) {
            return NONE; // "Negotiation": no Cylon ship launches or activates for the rest of the turn
        }
        val basestar = galacticaBoard.shipInSpace(event.basestarShipId(), BasestarView.class);
        if (basestar.damage().contains(DISABLED_WEAPONS)) {
            return NONE; // "Disabled Weapons": "the basestar may not attack Galactica"
        }
        return single(new AttackGalacticaEvent(basestar.id()));
    }
}
