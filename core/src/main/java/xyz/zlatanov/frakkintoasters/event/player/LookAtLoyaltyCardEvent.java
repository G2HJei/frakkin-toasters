package xyz.zlatanov.frakkintoasters.event.player;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;

public record LookAtLoyaltyCardEvent(int playerNumber, int targetPlayerNumber) implements PlayerEvent {
}
