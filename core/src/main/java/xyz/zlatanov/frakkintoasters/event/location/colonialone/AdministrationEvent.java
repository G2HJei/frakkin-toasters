package xyz.zlatanov.frakkintoasters.event.location.colonialone;

import xyz.zlatanov.frakkintoasters.event.LocationEvent;
import xyz.zlatanov.frakkintoasters.state.board.Location;

import static xyz.zlatanov.frakkintoasters.state.board.Location.ADMINISTRATION;

// Administration (Colonial One location, Daybreak)
// Action: Draw 1 Mutiny Card. If the President has any Mutiny Cards, choose a player to gain the President title. If
// the "Accept Prophecy" card is in play, the President may discard it to keep his title.
public record AdministrationEvent(int playerNumber) implements LocationEvent {

    @Override
    public Location location() {
        return ADMINISTRATION;
    }
}
