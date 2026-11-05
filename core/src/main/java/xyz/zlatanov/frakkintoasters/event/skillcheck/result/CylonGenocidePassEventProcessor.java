package xyz.zlatanov.frakkintoasters.event.skillcheck.result;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.ship.CylonShip;

public class CylonGenocidePassEventProcessor extends EventProcessor<CylonGenocidePassEvent> {

    // Centurions are not ships, and ships on the Cylon Fleet board are not on the main game board.
    @Override
    public Followup process() {
        galacticaBoard.shipsInSpace(CylonShip.class)
                .forEach(ship -> ops.ship(ship.id()).destroy());
        return Followup.NONE;
    }
}
