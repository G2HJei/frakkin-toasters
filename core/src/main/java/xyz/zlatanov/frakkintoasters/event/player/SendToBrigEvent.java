package xyz.zlatanov.frakkintoasters.event.player;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;

// Send a character to the "Brig". The player with playerNumber sends the character of targetPlayerNumber.
public record SendToBrigEvent(int playerNumber, int targetPlayerNumber) implements PlayerEvent {
}
