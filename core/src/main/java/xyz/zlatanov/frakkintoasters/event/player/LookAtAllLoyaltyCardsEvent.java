package xyz.zlatanov.frakkintoasters.event.player;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;

// The player looks at every Loyalty Card of another player that isn't revealed, e.g. Gaius Baltar's "Cylon Detector".
public record LookAtAllLoyaltyCardsEvent(int playerNumber, int targetPlayerNumber) implements PlayerEvent {
}
