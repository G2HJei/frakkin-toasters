package xyz.zlatanov.frakkintoasters.event.location.galactica;

import xyz.zlatanov.frakkintoasters.event.AnywhereLocationEvent;
import xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint;
import xyz.zlatanov.frakkintoasters.state.board.Location;

import java.util.List;

import static xyz.zlatanov.frakkintoasters.state.board.Location.ARMORY;

// Armory (Galactica location)
// Action: Attack a centurion on the Boarding Party track (destroyed on roll of 7-8).
public record ArmoryEvent(int playerNumber, int centurionId, boolean anywhere) implements AnywhereLocationEvent {

    public ArmoryEvent(int playerNumber, int centurionId) {
        this(playerNumber, centurionId, false);
    }

    @Override
    public Location location() {
        return ARMORY;
    }

    @Override
    public ArmoryEvent withConstraints(List<EventConstraint> constraints) {
        return new ArmoryEvent(playerNumber, centurionId, AnywhereLocationEvent.anywhere(constraints));
    }
}
