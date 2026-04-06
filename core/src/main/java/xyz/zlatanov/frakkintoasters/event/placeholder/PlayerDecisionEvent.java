package xyz.zlatanov.frakkintoasters.event.placeholder;

import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.PlayerEvent;

public record PlayerDecisionEvent(int playerNumber, Class<? extends Event> action) implements PlayerEvent {
}
