package xyz.zlatanov.frakkintoasters.event.action.character;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;

public record EndRemoteActionEvent(int playerNumber) implements PlayerEvent {
}
