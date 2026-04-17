package xyz.zlatanov.frakkintoasters.event;

import xyz.zlatanov.frakkintoasters.event.placeholder.decisionconstraint.DecisionConstraint;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.exception.InvalidActionException;

import java.util.List;

public interface Event<T extends Event<T>> {

    default List<Followup> execute(Game game) {
        return execute(game, null);
    }

    default List<Followup> execute(Game game, DecisionConstraint<T> decisionConstraint) {
        if (decisionConstraint != null && !decisionConstraint.validConstraint(self())) {
            throw new InvalidActionException("Decision constraint violation!");
        }
        if (!isValid(game)) {
            throw new InvalidActionException("Invalid action!");
        }
        return apply(game);
    }

    default boolean isValid(Game game) {
        return true;
    }

    default List<Followup> apply(Game game) {
        return List.of();
    }

    @SuppressWarnings("unchecked")
    default T self() {
        return (T) this;
    }
}