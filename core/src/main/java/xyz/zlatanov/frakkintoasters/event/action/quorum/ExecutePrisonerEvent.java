package xyz.zlatanov.frakkintoasters.event.action.quorum;

import xyz.zlatanov.frakkintoasters.event.action.QuorumCardEvent;

// Execute Prisoner (Quorum Card)
// Action: a character in the "Brig" is executed, then the card is discarded.
public record ExecutePrisonerEvent(int playerNumber, int targetPlayerNumber) implements QuorumCardEvent {
}
