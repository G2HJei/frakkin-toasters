package xyz.zlatanov.frakkintoasters;

import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.Player;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;
import xyz.zlatanov.frakkintoasters.state.exception.InvalidActionException;

public abstract class EventProcessor<T extends Event> {

    protected Game game; //todo make private and use only utility metohds?
    protected T    event;

    public final Followup execute(Game game, T event) {
        setContext(game, event);
        init();
        validate();
        return process();
    }

    public abstract Followup process();

    protected void init() {

    }

    protected boolean isValid() {
        return true;
    }

    private void setContext(Game game, T event) {
        assert this.game == null && this.event == null;
        this.game = game;
        this.event = event;
    }

    private void validate() {
        if (!isValid()) {
            throw new InvalidActionException("Invalid action!");
        }
    }

    //todo maybe instead of utility methods use utility fields for a lot of stuff?
    protected Player player() {
        if (event instanceof PlayerEvent playerEvent) {
            return game.player(playerEvent.playerNumber());
        } else {
            throw new FrakCallTheAdmiralException("Cannot find player for non-player event");
        }
    }
}
