package xyz.zlatanov.frakkintoasters.event.skillcheck.result;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.ship.DestroyCylonShipsEvent;
import xyz.zlatanov.frakkintoasters.state.ship.CylonShip;

import static xyz.zlatanov.frakkintoasters.state.board.LocationsArea.GALACTICA_SPACE;

public class StrangeBeaconPassEventProcessor extends EventProcessor<StrangeBeaconPassEvent> {

    // The current player chooses one of the space areas that has Cylon ships (nobody chooses when there are none).
    @Override
    public Followup process() {
        return Followup.one(GALACTICA_SPACE.locations().stream()
                .filter(area -> !galacticaBoard.shipsIn(area, CylonShip.class).isEmpty())
                .map(DestroyCylonShipsEvent::new)
                .toArray(Event[]::new));
    }
}
