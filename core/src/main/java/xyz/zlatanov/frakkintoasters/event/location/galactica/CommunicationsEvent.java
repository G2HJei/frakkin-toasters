package xyz.zlatanov.frakkintoasters.event.location.galactica;

import xyz.zlatanov.frakkintoasters.event.AnywhereLocationEvent;
import xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint;
import xyz.zlatanov.frakkintoasters.state.board.Location;

import java.util.List;

import static xyz.zlatanov.frakkintoasters.state.board.Location.COMMUNICATIONS;

// Communications (Galactica location)
// Action: Look at the back of 2 civilian ships. You may then move them to adjacent area(s).
public record CommunicationsEvent(int playerNumber, Integer civilianShipId1, Integer civilianShipId2,
                                  boolean anywhere) implements AnywhereLocationEvent {

    public CommunicationsEvent(int playerNumber, Integer civilianShipId1, Integer civilianShipId2) {
        this(playerNumber, civilianShipId1, civilianShipId2, false);
    }

    @Override
    public Location location() {
        return COMMUNICATIONS;
    }

    @Override
    public CommunicationsEvent withConstraints(List<EventConstraint> constraints) {
        return new CommunicationsEvent(playerNumber, civilianShipId1, civilianShipId2,
                AnywhereLocationEvent.anywhere(constraints));
    }
}
