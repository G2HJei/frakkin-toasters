package xyz.zlatanov.frakkintoasters;

import lombok.RequiredArgsConstructor;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;
import xyz.zlatanov.frakkintoasters.state.exception.InvalidActionException;

import java.util.List;

@RequiredArgsConstructor
public abstract class EventProcessor<T extends Event> {

    protected final Game game;
    protected final T    event;

    public Followup execute() {
        validateConstraints();
        validateEvent();
        return apply();
    }

    List<EventConstraint> eventConstraints() {
        return List.of();
    }

    boolean isValid() {
        return true;
    }

    abstract Followup apply();

    boolean isValidConstraint(EventConstraint constraint) {
        throw new FrakCallTheAdmiralException();
    }

    private void validateConstraints() {
        for (var constraint : eventConstraints()) {
            if (!isValidConstraint(constraint)) {
                throw new InvalidActionException("Invalid action!");
            }
        }
    }

    private void validateEvent() {
        if (!isValid()) {
            throw new InvalidActionException("Invalid action!");
        }
    }
}
