package xyz.zlatanov.frakkintoasters.event.action.quorum;

import xyz.zlatanov.frakkintoasters.event.action.QuorumCardEvent;
import xyz.zlatanov.frakkintoasters.state.board.Location;

// Presidential Pardon (Quorum Card)
// Action: another character leaves the "Brig" for another location on Galactica. The card prints no discard, and is
// discarded like every card that doesn't stay in play.
public record PresidentialPardonEvent(int playerNumber, int targetPlayerNumber, Location destination)
        implements QuorumCardEvent {
}
