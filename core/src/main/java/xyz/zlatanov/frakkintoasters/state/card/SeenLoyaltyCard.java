package xyz.zlatanov.frakkintoasters.state.card;

// A Loyalty Card of another player that a player looked at, e.g. after passing "Informing the Public".
public record SeenLoyaltyCard(int ownerPlayerNumber, LoyaltyCard card) {
}
