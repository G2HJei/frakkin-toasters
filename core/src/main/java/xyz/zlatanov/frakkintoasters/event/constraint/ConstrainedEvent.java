package xyz.zlatanov.frakkintoasters.event.constraint;

import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;

import java.util.List;

public interface ConstrainedEvent extends Event {

    ConstrainedEvent withConstraints(List<EventConstraint> constraints);

    static EventConstraint atMostOne(List<EventConstraint> constraints) {
        return switch (constraints.size()) {
            case 0 -> null;
            case 1 -> constraints.getFirst();
            default -> throw new FrakCallTheAdmiralException("Only one constraint fits: " + constraints);
        };
    }
}
