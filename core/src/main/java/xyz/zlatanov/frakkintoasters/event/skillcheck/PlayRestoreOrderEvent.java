package xyz.zlatanov.frakkintoasters.event.skillcheck;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;

public record PlayRestoreOrderEvent(int playerNumber) implements PlayerEvent {
}
