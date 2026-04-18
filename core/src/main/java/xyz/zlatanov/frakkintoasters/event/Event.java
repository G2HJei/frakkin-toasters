package xyz.zlatanov.frakkintoasters.event;

import xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.exception.InvalidActionException;

import java.util.List;

public interface Event {

    default List<Followup> execute(Game game) {
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

    default EventConstraint eventConstraint() {
        return null;
    }
}
