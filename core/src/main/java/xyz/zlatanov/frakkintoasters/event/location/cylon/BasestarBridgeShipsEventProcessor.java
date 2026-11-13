package xyz.zlatanov.frakkintoasters.event.location.cylon;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

import static xyz.zlatanov.frakkintoasters.state.board.LocationsArea.CYLON_FLEET_SPACE;
import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.BASESTAR;
import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.RAIDER;

public class BasestarBridgeShipsEventProcessor extends EventProcessor<BasestarBridgeShipsEvent> {

    private static final int RAIDERS = 3;

    @Override
    protected boolean isValid() {
        val shipType = event.shipType();
        return (shipType == BASESTAR || shipType == RAIDER)
                && event.location() != null
                && CYLON_FLEET_SPACE.locations().contains(event.location());
    }

    // "If ... both basestars are already on either the main game board or the Cylon Fleet game board, nothing happens."
    // (rule 9.7) The raiders left in the supply, if fewer than 3.
    @Override
    public Followup process() {
        val shipType = event.shipType();
        val count = Math.min(shipType == BASESTAR ? 1 : RAIDERS, game.cylonShips().available(shipType));
        for (int i = 0; i < count; i++) {
            ops.cylonFleet().place(shipType, event.location());
        }
        return Followup.NONE;
    }
}
