package xyz.zlatanov.frakkintoasters.event.jump;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

public class MoveCylonShipsToFleetBoardEventProcessor extends EventProcessor<MoveCylonShipsToFleetBoardEvent> {

    @Override
    public Followup process() {
        ops.cylonFleet().moveShipsFromGalactica();
        return Followup.NONE;
    }
}
