package xyz.zlatanov.frakkintoasters.event.action.skillcard;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.state.card.QuorumCard;

// After "Popular Influence": one drawn Quorum Card goes to the President, and the player holds the other to play or
// discard. It is only ever an option the server builds.
public record PopularInfluenceChoiceEvent(int playerNumber, QuorumCard given, QuorumCard other) implements PlayerEvent {
}
