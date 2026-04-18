package xyz.zlatanov.frakkintoasters.event;

import xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;
import xyz.zlatanov.frakkintoasters.state.exception.InvalidActionException;

import java.util.List;

public interface Event {

    default List<Followup> execute(Game game) {
        for (var constraint : eventConstraints()) {
            if (!supportedConstraints().contains(constraint)) {
                throw new FrakCallTheAdmiralException(
                        "Unsupported constraint " + constraint + " for " + getClass().getSimpleName());
            }
            if (!validConstraint(game, constraint)) {
                throw new InvalidActionException("Invalid action!");
            }
        }
        if (!isValid(game)) {
            throw new InvalidActionException("Invalid action!");
        }
        return apply(game);
    }

    default boolean isValid(Game game) {
        return true;
    }

    default boolean validConstraint(Game game, EventConstraint constraint) {
        return true;
    }

    default List<Followup> apply(Game game) {
        return List.of();
    }

    default List<EventConstraint> eventConstraints() {
        return List.of();
    }

    default List<EventConstraint> supportedConstraints() {
        return List.of();
    }
}
