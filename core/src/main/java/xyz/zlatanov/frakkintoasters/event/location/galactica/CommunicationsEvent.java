package xyz.zlatanov.frakkintoasters.event.location.galactica;

import xyz.zlatanov.frakkintoasters.event.ActionEvent;
import xyz.zlatanov.frakkintoasters.state.board.Location;

import static xyz.zlatanov.frakkintoasters.state.board.Location.COMMUNICATIONS;

// Communications (Galactica location)
// Action: Look at the back of 2 civilian ships. You may then move them to adjacent area(s).
public record CommunicationsEvent(int playerNumber, Integer civilianShipId1,
                                  Integer civilianShipId2) implements ActionEvent {

    @Override
    public Location location() {
        return COMMUNICATIONS;
    }
}
