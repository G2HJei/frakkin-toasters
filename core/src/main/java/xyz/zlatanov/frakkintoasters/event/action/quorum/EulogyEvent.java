package xyz.zlatanov.frakkintoasters.event.action.quorum;

import xyz.zlatanov.frakkintoasters.event.action.QuorumCardEvent;

// Eulogy (Quorum Card)
// Action: if an execution has cost the fleet morale, the fleet gains 1 morale. Then the card is discarded.
public record EulogyEvent(int playerNumber) implements QuorumCardEvent {
}
