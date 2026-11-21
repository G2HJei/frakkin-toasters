package xyz.zlatanov.frakkintoasters.event.action.quorum;

import xyz.zlatanov.frakkintoasters.event.action.QuorumCardEvent;

// Enact Production Quotas (Quorum Card)
// Action: the fleet gains 1 food and loses 1 morale, then the card is discarded.
public record EnactProductionQuotasEvent(int playerNumber) implements QuorumCardEvent {
}
