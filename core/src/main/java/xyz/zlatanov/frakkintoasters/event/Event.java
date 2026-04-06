package xyz.zlatanov.frakkintoasters.event;

import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.exception.InvalidActionException;

public interface Event {

    default EventFollowup execute(Game game) {
        if (!isValid(game)) {
            throw new InvalidActionException("Invalid action!");
        }
        apply(game);
        return followup(game);
    }

    default boolean isValid(Game game) {
        return true;
    }

    void apply(Game game);

    default EventFollowup followup(Game game) {
        return null;
    }
}
