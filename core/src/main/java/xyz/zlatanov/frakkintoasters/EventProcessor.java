package xyz.zlatanov.frakkintoasters;

import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.Player;
import xyz.zlatanov.frakkintoasters.state.exception.EventConstraintViolationException;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;
import xyz.zlatanov.frakkintoasters.state.exception.InvalidActionException;

import java.util.List;

public abstract class EventProcessor<T extends Event> {

    protected Game game; //todo make private and use only utility metohds?
    protected T    event;

    public final Followup execute(Game game, T event) {
        setContext(game, event);
        init();
        validateConstraints();
        validateEvent();
        return processEvent();
    }

    public abstract Followup processEvent();

    protected void init() {

    }

    protected List<EventConstraint> eventConstraints() {
        return List.of();
    }

    protected boolean isValidConstraint(EventConstraint constraint) {
        throw new FrakCallTheAdmiralException();
    }

    protected boolean isValid() {
        return true;
    }

    private void setContext(Game game, T event) {
        assert this.game == null && this.event == null;
        this.game = game;
        this.event = event;
    }

    private void validateConstraints() {
        for (var constraint : eventConstraints()) {
            if (!isValidConstraint(constraint)) {
                throw new EventConstraintViolationException(constraint.name());
            }
        }
    }

    private void validateEvent() {
        if (!isValid()) {
            throw new InvalidActionException("Invalid action!");
        }
    }

    protected Player player() {
        if (event instanceof PlayerEvent playerEvent) {
            return game.player(playerEvent.playerNumber());
        } else {
            throw new FrakCallTheAdmiralException("Cannot find player for non-player event");
        }
    }
}
