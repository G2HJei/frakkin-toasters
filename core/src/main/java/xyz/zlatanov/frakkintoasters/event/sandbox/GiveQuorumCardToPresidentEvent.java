package xyz.zlatanov.frakkintoasters.event.sandbox;

import xyz.zlatanov.frakkintoasters.state.card.QuorumCard;

public record GiveQuorumCardToPresidentEvent(QuorumCard card) implements SandboxEvent {
}
