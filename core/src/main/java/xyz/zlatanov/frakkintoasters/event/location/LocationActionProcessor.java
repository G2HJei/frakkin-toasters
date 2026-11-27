package xyz.zlatanov.frakkintoasters.event.location;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.AnywhereLocationEvent;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.LocationEvent;
import xyz.zlatanov.frakkintoasters.event.character.ActionCosts;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.discard;

/**
 * A player whose ability puts a cost on activating a location ({@link ActionCosts#locationCost}) pays it first: the
 * discard, then the same event again, which takes the action.
 */
public abstract class LocationActionProcessor<T extends LocationEvent> extends EventProcessor<T> {

    @Override
    protected final boolean isValid() {
        // The cost is paid and the action follows. process() checks again whether he can still take it.
        return game.turn().actionCostPaid(player.number()) || canTakeAction();
    }

    @Override
    public final Followup process() {
        val playerNumber = player.number();
        if (game.turn().actionCostPaid(playerNumber)) {
            val canStillAct = canTakeAction();
            ops.actionCostSettled();
            // e.g. a Treachery Card he discarded drew him a second Mutiny Card, and he went to the "Brig"
            return canStillAct ? act() : NONE;
        }
        val cost = ActionCosts.locationCost(player, event.location());
        if (cost > 0) {
            ops.payActionCost(playerNumber);
            return all(discard(playerNumber, cost), event);
        }
        return act();
    }

    protected boolean isValidAction() {
        return true;
    }

    protected abstract Followup act();

    private boolean canTakeAction() {
        val location = event.location();
        if (event instanceof AnywhereLocationEvent anywhereEvent && anywhereEvent.anywhere()) {
            // "Unorthodox Plan": from anywhere, even when the location is damaged.
            return LocationEvent.canActIgnoringDamage(game, player, location) && isValidAction();
        }
        val remote = game.turn().remoteActionLocation(player.number());
        if (remote.isPresent()) {
            // An ability lets him take the action of that location from wherever he is, e.g. "Organized": only that one.
            return remote.get() == location && LocationEvent.canAct(game, player, location) && isValidAction();
        }
        val atLocation = game.locate(player.character()).filter(location::equals).isPresent();
        return atLocation && LocationEvent.canAct(game, player, location) && isValidAction();
    }
}
