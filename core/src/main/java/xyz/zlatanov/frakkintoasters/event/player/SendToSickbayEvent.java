package xyz.zlatanov.frakkintoasters.event.player;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;

public record SendToSickbayEvent(int playerNumber, int targetPlayerNumber) implements PlayerEvent {
}
