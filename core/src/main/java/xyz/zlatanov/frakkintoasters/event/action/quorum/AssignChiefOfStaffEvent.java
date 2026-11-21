package xyz.zlatanov.frakkintoasters.event.action.quorum;

import xyz.zlatanov.frakkintoasters.event.action.QuorumCardEvent;

// Assign Chief of Staff (Quorum Card)
// Action: the player draws 2 Politics Cards and gives the card to another player, who keeps it in play. Before cards
// go into a skill check, the holder may discard it to make the Politics Cards in it count positive.
public record AssignChiefOfStaffEvent(int playerNumber, int holderPlayerNumber) implements QuorumCardEvent {
}
