package xyz.zlatanov.frakkintoasters.event;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.state.ship.Basestar;

import java.util.Comparator;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.state.board.LocationsArea.GALACTICA_SPACE;
import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.BASESTAR;

public class ActivateCylonBasestarsEventProcessor extends EventProcessor<ActivateCylonBasestarsEvent> {

    @Override
    public Followup process() {
        val galactica = game.boards().galactica();
        val basestars = galactica.shipsInSpace(Basestar.class);

        if (basestars.isEmpty()) {
            return all(new PlaceShipOnCylonFleetBoardEvent(BASESTAR), new AdvancePursuitTrackEvent());
        }

        return all(basestars.stream()
                //todo return in clockwise order always (in galactica board class)
                .sorted(Comparator.comparingInt(b -> GALACTICA_SPACE.locations().indexOf(galactica.locate(b))))
                .map(b -> new ActivateCylonBasestarEvent(b.id()))
                .toArray(Event[]::new));
    }
}
