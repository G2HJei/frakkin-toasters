package xyz.zlatanov.frakkintoasters.event.sandbox;

import xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard;

// Gives a player a Loyalty Card face down: out of the Loyalty deck, out of the "You Are Not a Cylon" deck, or a "You
// Are a Cylon" card nobody has yet. Unlike a deal, the Mutineer card isn't revealed.
public record GiveLoyaltyCardToPlayerEvent(int targetPlayerNumber, LoyaltyCard card) implements SandboxEvent {
}
