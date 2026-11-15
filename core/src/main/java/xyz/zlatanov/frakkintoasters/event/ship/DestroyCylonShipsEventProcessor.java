package xyz.zlatanov.frakkintoasters.event.ship;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.board.LocationsArea;
import xyz.zlatanov.frakkintoasters.state.ship.CylonShip;

public class DestroyCylonShipsEventProcessor extends EventProcessor<DestroyCylonShipsEvent> {

    @Override
    protected boolean isValid() {
        return event.area() != null && LocationsArea.GALACTICA_SPACE.locations().contains(event.area());
    }

    @Override
    public Followup process() {
        // Centurions are not ships.
        galacticaBoard.shipsIn(event.area(), CylonShip.class)
                .forEach(ship -> ops.ship(ship.id()).destroy());
        return Followup.NONE;
    }
}
