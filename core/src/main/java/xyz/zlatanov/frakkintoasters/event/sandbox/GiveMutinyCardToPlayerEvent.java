package xyz.zlatanov.frakkintoasters.event.sandbox;

import xyz.zlatanov.frakkintoasters.state.card.MutinyCard;

// Gives a player a Mutiny Card out of the Mutiny draw pile. Unlike a draw, it never sends him to the "Brig".
public record GiveMutinyCardToPlayerEvent(int targetPlayerNumber, MutinyCard card) implements SandboxEvent {
}
