package xyz.zlatanov.frakkintoasters.event;

import xyz.zlatanov.frakkintoasters.state.board.Location;

/**
 * An action a player takes in the Action step: all location actions. The acting player is not always the current
 * player (e.g. the target of an "Executive Order" card gets a bonus action), so it is carried by the event.
 */
public interface ActionEvent extends PlayerEvent {

    /**
     * @return the location whose action this is. The player has to be there, and it can't be damaged
     */
    Location location(); //todo create LocationEvent interface instead as actions can be something else than an action
}
