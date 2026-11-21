package xyz.zlatanov.frakkintoasters.event.action.skillcard;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.state.card.QuorumCard;

// The player discards the Quorum Card "Popular Influence" left him, face up.
public record DiscardHeldQuorumCardEvent(int playerNumber, QuorumCard card) implements PlayerEvent {
}
