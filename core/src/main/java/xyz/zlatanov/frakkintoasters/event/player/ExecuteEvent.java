package xyz.zlatanov.frakkintoasters.event.player;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;

// Execute the player's character, e.g. after passing the "Airlock" skill check.
public record ExecuteEvent(int playerNumber) implements PlayerEvent {
}
