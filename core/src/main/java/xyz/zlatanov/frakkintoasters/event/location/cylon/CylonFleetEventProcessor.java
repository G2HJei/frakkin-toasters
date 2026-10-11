package xyz.zlatanov.frakkintoasters.event.location.cylon;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.crisis.ActivateHeavyRaidersAndCenturionsEvent;
import xyz.zlatanov.frakkintoasters.event.crisis.ActivateRaidersEvent;
import xyz.zlatanov.frakkintoasters.state.view.BasestarView;

import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.HEAVY_RAIDER;
import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.RAIDER;

public class CylonFleetEventProcessor extends EventProcessor<CylonFleetEvent> {

    @Override
    public boolean isValid() {
        val shipType = event.typeToActivate();
        return shipType == null
                || shipType == RAIDER
                || shipType == HEAVY_RAIDER;
    }

    @Override
    public Followup process() {
        val shipType = event.typeToActivate();
        if (shipType == null) {
            launch2RaidersAndHeavyRaider();
            return Followup.NONE;
        } else if (shipType == RAIDER) {
            return single(new ActivateRaidersEvent());
        } else {
            return single(new ActivateHeavyRaidersAndCenturionsEvent());
        }
    }


    private void launch2RaidersAndHeavyRaider() {
        for (val basestar : galacticaBoard.shipsInSpace(BasestarView.class)) {
            ops.ship(basestar.id()).launchRaiders(2);
            ops.ship(basestar.id()).launchHeavyRaider();
        }
    }
}
