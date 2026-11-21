package xyz.zlatanov.frakkintoasters.event.action.quorum;

import xyz.zlatanov.frakkintoasters.event.action.QuorumCardEvent;

// Establish Dogsville (Quorum Card)
// Action: the fleet gains 1 population and loses 1 morale, then the card is discarded.
public record EstablishDogsvilleEvent(int playerNumber) implements QuorumCardEvent {
}
