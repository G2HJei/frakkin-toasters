package xyz.zlatanov.frakkintoasters.event.location.galactica;

import xyz.zlatanov.frakkintoasters.event.AnywhereLocationEvent;
import xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint;
import xyz.zlatanov.frakkintoasters.state.board.Location;

import java.util.List;

import static xyz.zlatanov.frakkintoasters.state.board.Location.COMMAND;

// Command (Galactica location)
// Action: Activate up to 2 unmanned vipers.
public record CommandEvent(int playerNumber, boolean anywhere) implements AnywhereLocationEvent {

    public CommandEvent(int playerNumber) {
        this(playerNumber, false);
    }

    @Override
    public Location location() {
        return COMMAND;
    }

    @Override
    public CommandEvent withConstraints(List<EventConstraint> constraints) {
        return new CommandEvent(playerNumber, AnywhereLocationEvent.anywhere(constraints));
    }
}
