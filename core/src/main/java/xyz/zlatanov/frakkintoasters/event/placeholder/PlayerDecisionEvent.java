package xyz.zlatanov.frakkintoasters.event.placeholder;

import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint;

public record PlayerDecisionEvent<T extends Event>(
        int playerNumber,
        Class<T> action,
        EventConstraint eventConstraint
) implements PlayerEvent {

    public PlayerDecisionEvent(int playerNumber, Class<T> action) {
        this(playerNumber, action, null);
    }
}
