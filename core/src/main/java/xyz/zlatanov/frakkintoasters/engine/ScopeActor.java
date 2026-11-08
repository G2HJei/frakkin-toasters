package xyz.zlatanov.frakkintoasters.engine;

import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.state.view.GameView;

/**
 * The player an event's journal scope is logged under. A submitted event is logged under the player who submitted it,
 * so a {@code SendToBrigEvent} answered by the Admiral is the Admiral's, not the current player's.
 */
public final class ScopeActor {

    private ScopeActor() {
    }

    /**
     * @return for an event that was queued, not submitted: the player of a {@link PlayerEvent}, otherwise the acting
     * player
     */
    public static int ofQueued(GameView game, Event event) {
        return event instanceof PlayerEvent playerEvent
                ? playerEvent.playerNumber()
                : game.turn().actingPlayerNumber();
    }
}
