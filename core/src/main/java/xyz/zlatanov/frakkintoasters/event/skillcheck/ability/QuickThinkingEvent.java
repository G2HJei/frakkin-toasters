package xyz.zlatanov.frakkintoasters.event.skillcheck.ability;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;

public record QuickThinkingEvent(int playerNumber) implements PlayerEvent {
}
