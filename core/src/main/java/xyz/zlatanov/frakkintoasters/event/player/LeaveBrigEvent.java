package xyz.zlatanov.frakkintoasters.event.player;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.event.constraint.ConstrainedEvent;
import xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint;
import xyz.zlatanov.frakkintoasters.state.board.Location;

import java.util.List;

public record LeaveBrigEvent(int playerNumber, Location destination,
                             EventConstraint destinationLimit) implements PlayerEvent, ConstrainedEvent {

    public LeaveBrigEvent(int playerNumber, Location destination) {
        this(playerNumber, destination, null);
    }

    @Override
    public LeaveBrigEvent withConstraints(List<EventConstraint> constraints) {
        return new LeaveBrigEvent(playerNumber, destination, ConstrainedEvent.atMostOne(constraints));
    }
}
