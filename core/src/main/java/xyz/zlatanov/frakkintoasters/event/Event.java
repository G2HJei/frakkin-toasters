package xyz.zlatanov.frakkintoasters.event;

import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.exception.InvalidActionException;

import java.util.List;

public interface Event {

    default List<Followup> execute(Game game) {
        if (!isValid(game)) {
            throw new InvalidActionException("Invalid action!");
        }
        apply(game);
        return followup(game);
    }

    default boolean isValid(Game game) {
        return true;
    }

    default void apply(Game game) {
    }

    default List<Followup> followup(Game game) {
        return List.of();
    }
}
