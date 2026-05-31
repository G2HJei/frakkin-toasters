package xyz.zlatanov.frakkintoasters;

import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.exception.EventConstraintViolationException;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;
import xyz.zlatanov.frakkintoasters.state.exception.InvalidActionException;

import java.util.List;

public abstract class EventProcessor<T extends Event> {

    protected Game game; //todo make private and use only utility metohds?
    protected T    event;

    public Followup execute(Game game, T event) {
        setContext(game, event);
        validateConstraints();
        validateEvent();
        return processEvent();
    }

    public List<EventConstraint> eventConstraints() {
        return List.of();
    }

    //todo tune access levels
    public boolean isValidConstraint(EventConstraint constraint) {
        throw new FrakCallTheAdmiralException();
    }

    public boolean isValid() {
        return true;
    }

    public abstract Followup processEvent();

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
}
