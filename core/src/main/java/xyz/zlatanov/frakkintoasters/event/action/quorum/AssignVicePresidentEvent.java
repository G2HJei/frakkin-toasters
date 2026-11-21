package xyz.zlatanov.frakkintoasters.event.action.quorum;

import xyz.zlatanov.frakkintoasters.event.action.QuorumCardEvent;

// Assign Vice President (Quorum Card)
// Action: the player draws 2 Politics Cards and gives the card to another player, who keeps it in play. While the
// holder isn't President, nobody else can be chosen at the "Administration".
public record AssignVicePresidentEvent(int playerNumber, int holderPlayerNumber) implements QuorumCardEvent {
}
