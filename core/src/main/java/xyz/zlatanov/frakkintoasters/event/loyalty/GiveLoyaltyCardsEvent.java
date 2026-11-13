package xyz.zlatanov.frakkintoasters.event.loyalty;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;

// A revealed Cylon gives all his facedown Loyalty Cards to a human player of his choice (rules 6.1.1, 7.1).
public record GiveLoyaltyCardsEvent(int playerNumber, int recipientPlayerNumber) implements PlayerEvent {
}
