package xyz.zlatanov.frakkintoasters.event.action.quorum;

import xyz.zlatanov.frakkintoasters.event.action.QuorumCardEvent;

// Inspirational Speech (Quorum Card)
// Action: roll a die. On 6 or more the fleet gains 1 morale and the card leaves the game, otherwise it is discarded.
public record InspirationalSpeechEvent(int playerNumber) implements QuorumCardEvent {
}
