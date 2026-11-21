package xyz.zlatanov.frakkintoasters.event.action.character;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;

// The action an ability let the player take at a location from anywhere is over.
public record EndRemoteActionEvent(int playerNumber) implements PlayerEvent {
}
