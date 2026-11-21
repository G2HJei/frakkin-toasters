package xyz.zlatanov.frakkintoasters.event.action.quorum;

import xyz.zlatanov.frakkintoasters.event.action.QuorumCardEvent;

// Food Rationing (Quorum Card)
// Action: roll a die. On 6 or more the fleet gains 1 food and the card leaves the game, otherwise it is discarded.
public record FoodRationingEvent(int playerNumber) implements QuorumCardEvent {
}
