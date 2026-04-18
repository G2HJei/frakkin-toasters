package xyz.zlatanov.frakkintoasters.event.placeholder;

import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint;

import java.util.List;

public record PlayerDecisionEvent<T extends Event>(
        int playerNumber,
        Class<T> action,
        List<EventConstraint> eventConstraints
) implements PlayerEvent {

    public PlayerDecisionEvent(int playerNumber, Class<T> action) {
        this(playerNumber, action, List.of());
    }

    public PlayerDecisionEvent(int playerNumber, Class<T> action, EventConstraint... eventConstraints) {
        this(playerNumber, action, List.of(eventConstraints));
    }
}
