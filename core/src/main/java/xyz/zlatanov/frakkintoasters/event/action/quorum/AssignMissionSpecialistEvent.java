package xyz.zlatanov.frakkintoasters.event.action.quorum;

import xyz.zlatanov.frakkintoasters.event.action.QuorumCardEvent;

// Assign Mission Specialist (Quorum Card)
// Action: the player draws 2 Politics Cards and gives the card to another player, who keeps it in play. At the next
// jump the holder draws 3 Destination Cards and chooses the destination instead of the Admiral, then discards it.
public record AssignMissionSpecialistEvent(int playerNumber, int holderPlayerNumber) implements QuorumCardEvent {
}
