package xyz.zlatanov.frakkintoasters.event.action;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.ActivateHeavyRaidersAndCenturionsAction;
import xyz.zlatanov.frakkintoasters.event.ActivateRaidersEvent;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.ship.Basestar;
import xyz.zlatanov.frakkintoasters.state.ship.Ship;

import java.util.ArrayList;

import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.HEAVY_RAIDER;
import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.RAIDER;

public class CylonFleetActionEventProcessor extends EventProcessor<CylonFleetActionEvent> {

    @Override
    public boolean isValid() {
        val shipType = event.typeToActivate();
        return shipType == null
                || shipType == RAIDER
                || shipType == HEAVY_RAIDER;
    }

    @Override
    public Followup processEvent() {
        val shipType = event.typeToActivate();
        if (shipType == null) {
            launch2RaidersAndHeavyRaider();
            return Followup.NONE;
        } else if (shipType == RAIDER) {
            return single(new ActivateRaidersEvent());
        } else {
            return single(new ActivateHeavyRaidersAndCenturionsAction());
        }
    }


    private void launch2RaidersAndHeavyRaider() {
        val galactica = game.boards().galactica();
        for (val basestar : galactica.shipsInSpace(Basestar.class)) {
            val location = galactica.locate(basestar);
            val shipsToPlace = getShips();
            galactica.place(location, shipsToPlace);
        }
    }

    private ArrayList<Ship> getShips() {
        val shipsToPlace = new ArrayList<Ship>();
        game.cylonShips().raider().ifPresent(shipsToPlace::add);
        game.cylonShips().raider().ifPresent(shipsToPlace::add);
        game.cylonShips().heavyRaider().ifPresent(shipsToPlace::add);
        return shipsToPlace;
    }
}
