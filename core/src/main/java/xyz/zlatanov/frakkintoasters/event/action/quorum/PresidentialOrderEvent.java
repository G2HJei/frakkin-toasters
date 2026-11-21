package xyz.zlatanov.frakkintoasters.event.action.quorum;

import xyz.zlatanov.frakkintoasters.event.action.QuorumCardEvent;
import xyz.zlatanov.frakkintoasters.state.Title;

// Presidential Order (Quorum Card)
// Action: a player gets the Admiral or the CAG title, then the card is discarded.
public record PresidentialOrderEvent(int playerNumber, int targetPlayerNumber, Title title) implements QuorumCardEvent {
}
