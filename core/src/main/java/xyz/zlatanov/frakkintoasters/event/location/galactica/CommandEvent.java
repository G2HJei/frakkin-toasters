package xyz.zlatanov.frakkintoasters.event.location.galactica;

import xyz.zlatanov.frakkintoasters.event.LocationEvent;
import xyz.zlatanov.frakkintoasters.state.board.Location;

import static xyz.zlatanov.frakkintoasters.state.board.Location.COMMAND;

// Command (Galactica location)
// Action: Activate up to 2 unmanned vipers.
public record CommandEvent(int playerNumber) implements LocationEvent {

    @Override
    public Location location() {
        return COMMAND;
    }
}
