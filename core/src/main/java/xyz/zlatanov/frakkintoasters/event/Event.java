package xyz.zlatanov.frakkintoasters.event;

import xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;
import xyz.zlatanov.frakkintoasters.state.exception.InvalidActionException;

import java.util.List;

public interface Event {

    default List<Followup> execute(Game game) {
        validateConstraints(game);
        validateEvent(game);
        return apply(game);
    }

    default boolean isValid(Game game) {
        return true;
    }

    default boolean validateConstraint(Game game, EventConstraint constraint) {
        throw new FrakCallTheAdmiralException();
    }

    default List<Followup> apply(Game game) {
        return List.of();
    }

    default List<EventConstraint> eventConstraints() {
        return List.of();
    }

    private void validateConstraints(Game game) {
        for (var constraint : eventConstraints()) {
            if (!validateConstraint(game, constraint)) {
                throw new InvalidActionException("Invalid action!");
            }
        }
    }

    private void validateEvent(Game game) {
        if (!isValid(game)) {
            throw new InvalidActionException("Invalid action!");
        }
    }
}
