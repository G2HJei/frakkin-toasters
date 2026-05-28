package xyz.zlatanov.frakkintoasters.event.action;

import lombok.val;
import xyz.zlatanov.frakkintoasters.event.ActionEvent;
import xyz.zlatanov.frakkintoasters.event.ActivateHeavyRaidersAndCenturionsAction;
import xyz.zlatanov.frakkintoasters.event.ActivateRaidersEvent;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.ship.Basestar;
import xyz.zlatanov.frakkintoasters.state.ship.Ship;
import xyz.zlatanov.frakkintoasters.state.ship.ShipType;

import java.util.ArrayList;

import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.HEAVY_RAIDER;
import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.RAIDER;

public record CylonFleetActionEvent(int playerNumber, ShipType typeToActivate) implements ActionEvent {

    @Override
    public boolean isValid(Game game) {
        return typeToActivate == null
                || typeToActivate == RAIDER
                || typeToActivate == HEAVY_RAIDER;
    }

    @Override
    public Followup apply(Game game) {
        if (typeToActivate == null) {
            launch2RaidersAndHeavyRaider(game);
            return Followup.NONE;
        } else if (typeToActivate == RAIDER) {
            return single(new ActivateRaidersEvent());
        } else {
            return single(new ActivateHeavyRaidersAndCenturionsAction());
        }
    }

    private void launch2RaidersAndHeavyRaider(Game game) {
        val galactica = game.boards().galactica();
        for (val basestar : galactica.shipsInSpace(Basestar.class)) {
            val location = galactica.locate(basestar);
            val shipsToPlace = getShips(game);
            galactica.place(location, shipsToPlace);
        }
    }

    private ArrayList<Ship> getShips(Game game) {
        val shipsToPlace = new ArrayList<Ship>();
        game.cylonShips().raider().ifPresent(shipsToPlace::add);
        game.cylonShips().raider().ifPresent(shipsToPlace::add);
        game.cylonShips().heavyRaider().ifPresent(shipsToPlace::add);
        return shipsToPlace;
    }
}
