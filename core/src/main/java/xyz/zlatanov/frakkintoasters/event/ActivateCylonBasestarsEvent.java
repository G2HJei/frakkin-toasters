package xyz.zlatanov.frakkintoasters.event;

import lombok.val;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.ship.Basestar;

import java.util.Comparator;
import java.util.List;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.state.board.Location.LOCATION_AREAS;
import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.BASESTAR;

public record ActivateCylonBasestarsEvent() implements Event {

    private static final List<Location> SPACE_AREAS_CLOCKWISE = LOCATION_AREAS.get("Galactica space");

    @Override
    public Followup apply(Game game) {
        val galactica = game.boards().galactica();
        val basestars = galactica.shipsInSpace(Basestar.class);

        if (basestars.isEmpty()) {
            return all(new PlaceShipOnCylonFleetBoardEvent(BASESTAR), new AdvancePursuitTrackEvent());
        }

        return all(basestars.stream()
                //todo return in clockwise order always (in galactica board class)
                .sorted(Comparator.comparingInt(b -> SPACE_AREAS_CLOCKWISE.indexOf(galactica.locate(b))))
                .map(b -> new ActivateCylonBasestarEvent(b.id()))
                .toArray(Event[]::new));
    }
}
