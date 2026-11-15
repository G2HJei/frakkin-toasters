package xyz.zlatanov.frakkintoasters.event.ship;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.board.Location;

import java.util.Map;

import static xyz.zlatanov.frakkintoasters.state.board.Location.*;

public class PlaceShipOnCylonFleetBoardResultEventProcessor extends EventProcessor<PlaceShipOnCylonFleetBoardResultEvent> {

    private static final Map<Integer, Location> PLACEMENT = Map.of(
            1, CYLON_FLEET_SPACE_1,
            2, CYLON_FLEET_SPACE_2,
            3, CYLON_FLEET_SPACE_3,
            4, CYLON_FLEET_SPACE_4,
            5, CYLON_FLEET_SPACE_5_6,
            6, CYLON_FLEET_SPACE_5_6,
            7, CYLON_FLEET_SPACE_7_8,
            8, CYLON_FLEET_SPACE_7_8
    );

    @Override
    protected boolean isValid() {
        return event.cylonShipType() != null && game.cylonShips().available(event.cylonShipType()) > 0;
    }

    @Override
    public Followup process() {
        ops.cylonFleet().place(event.cylonShipType(), PLACEMENT.get(game.turn().dieRoll().result()));
        return Followup.NONE;
    }
}
