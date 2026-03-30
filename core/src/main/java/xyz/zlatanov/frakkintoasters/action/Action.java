package xyz.zlatanov.frakkintoasters.action;

import xyz.zlatanov.frakkintoasters.Game;
import xyz.zlatanov.frakkintoasters.state.exception.InvalidActionException;

import java.util.List;

public interface Action {

    default List<Action> execute(Game game) {
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

    default List<Action> followup(Game game) {
        return List.of();
    }
}
