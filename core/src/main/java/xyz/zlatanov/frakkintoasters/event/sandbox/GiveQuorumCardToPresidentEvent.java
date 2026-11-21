package xyz.zlatanov.frakkintoasters.event.sandbox;

import xyz.zlatanov.frakkintoasters.state.card.QuorumCard;

// Gives the President a Quorum Card out of the Quorum draw pile.
public record GiveQuorumCardToPresidentEvent(QuorumCard card) implements SandboxEvent {
}
