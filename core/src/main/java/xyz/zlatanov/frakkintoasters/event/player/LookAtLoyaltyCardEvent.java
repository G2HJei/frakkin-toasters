package xyz.zlatanov.frakkintoasters.event.player;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;

// The player looks at 1 random Loyalty Card of the target player.
public record LookAtLoyaltyCardEvent(int playerNumber, int targetPlayerNumber) implements PlayerEvent {
}
