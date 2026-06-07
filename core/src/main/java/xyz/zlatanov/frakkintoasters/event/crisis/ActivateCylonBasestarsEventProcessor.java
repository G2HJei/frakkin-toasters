package xyz.zlatanov.frakkintoasters.event.crisis;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.AdvancePursuitTrackEvent;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.PlaceShipOnCylonFleetBoardEvent;
import xyz.zlatanov.frakkintoasters.state.ship.Basestar;

import java.util.Comparator;

import static xyz.zlatanov.frakkintoasters.state.board.LocationsArea.GALACTICA_SPACE;
import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.BASESTAR;

public class ActivateCylonBasestarsEventProcessor extends EventProcessor<ActivateCylonBasestarsEvent> {

    @Override
    public Followup process() {
        val galactica = galacticaBoard;
        val basestars = galactica.shipsInSpace(Basestar.class);

        if (basestars.isEmpty()) {
            return Followup.all(
                    new PlaceShipOnCylonFleetBoardEvent(BASESTAR),
                    new AdvancePursuitTrackEvent());
        }

        return Followup.all(basestars.stream()
                .sorted(Comparator.comparingInt(b -> GALACTICA_SPACE.locations().indexOf(galactica.locate(b))))
                .map(b -> new ActivateCylonBasestarEvent(b.id()))
                .toArray(Event[]::new));
    }
}
