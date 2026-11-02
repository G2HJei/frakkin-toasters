package xyz.zlatanov.frakkintoasters.event.player;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint;
import xyz.zlatanov.frakkintoasters.state.board.Location;

// The player's character leaves the "Brig" and moves to the destination, e.g. after passing the "Brig" skill check.
// Supports the DESTINATION_GALACTICA constraint.
public record LeaveBrigEvent(int playerNumber, Location destination,
                             EventConstraint destinationLimit) implements PlayerEvent {

    public LeaveBrigEvent(int playerNumber, Location destination) {
        this(playerNumber, destination, null);
    }
}
