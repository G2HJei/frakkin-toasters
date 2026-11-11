package xyz.zlatanov.frakkintoasters.event.location.colonialone;

import xyz.zlatanov.frakkintoasters.event.LocationEvent;
import xyz.zlatanov.frakkintoasters.state.board.Location;

import static xyz.zlatanov.frakkintoasters.state.board.Location.PRESIDENTS_OFFICE;

// President's Office (Colonial One location, Daybreak)
// Action: Draw 2 politics Skill Cards.
public record PresidentsOfficeEvent(int playerNumber) implements LocationEvent {

    @Override
    public Location location() {
        return PRESIDENTS_OFFICE;
    }
}
