package xyz.zlatanov.frakkintoasters.event.placeholder;

import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.decisionconstraint.DecisionConstraint;

public record PlayerDecisionEvent<T extends Event<T>>(
        int playerNumber,
        Class<T> action,
        Class<? extends DecisionConstraint<T>> decisionConstraint
) implements PlayerEvent {

    public PlayerDecisionEvent(int playerNumber, Class<T> action) {
        this(playerNumber, action, null);
    }

    public PlayerDecisionEvent(int playerNumber, Class<T> action, Class<? extends DecisionConstraint<T>> decisionConstraint) {
        this.playerNumber = playerNumber;
        this.action = action;
        this.decisionConstraint = decisionConstraint;
    }
}
