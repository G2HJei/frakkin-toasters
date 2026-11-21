package xyz.zlatanov.frakkintoasters.event.action.quorum;

import xyz.zlatanov.frakkintoasters.event.action.QuorumCardEvent;

// Arrest Order (Quorum Card)
// Action: the chosen character goes to the "Brig", then the card is discarded.
public record ArrestOrderEvent(int playerNumber, int targetPlayerNumber) implements QuorumCardEvent {
}
