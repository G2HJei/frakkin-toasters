package xyz.zlatanov.frakkintoasters.event.location;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.LocationEvent;

/**
 * Runs the action of a location. The player has to be at the location, and {@link LocationEvent#canAct} has to allow
 * it, e.g. the location can't be damaged. Subclasses check the details of the event in {@link #isValidAction()}.
 */
public abstract class LocationActionProcessor<T extends LocationEvent> extends EventProcessor<T> {

    @Override
    protected final boolean isValid() {
        val location = event.location();
        val atLocation = game.locate(player.character()).filter(location::equals).isPresent();
        return atLocation && LocationEvent.canAct(game, player, location) && isValidAction();
    }

    protected boolean isValidAction() {
        return true;
    }
}
