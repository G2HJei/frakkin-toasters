package xyz.zlatanov.frakkintoasters.event.action.quorum;

import xyz.zlatanov.frakkintoasters.event.action.QuorumCardEvent;

// Assign Arbitrator (Quorum Card)
// Action: the player draws 2 Politics Cards and gives the card to another player, who keeps it in play. When the
// "Admiral's Quarters" is activated, the holder may discard it to raise or lower the difficulty by 3.
public record AssignArbitratorEvent(int playerNumber, int holderPlayerNumber) implements QuorumCardEvent {
}
