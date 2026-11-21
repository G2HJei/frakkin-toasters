package xyz.zlatanov.frakkintoasters.event;

import xyz.zlatanov.frakkintoasters.event.constraint.ConstrainedEvent;
import xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;

import java.util.List;

import static xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint.ANYWHERE_EVEN_IF_DAMAGED;

/**
 * The action of a location that an "Unorthodox Plan" lets a player take from anywhere, even when the location is
 * damaged. Only a decision that carries {@code ANYWHERE_EVEN_IF_DAMAGED} sets {@link #anywhere()}: the engine replaces a
 * submitted event's constraints with the decision's, so a client can't set it.
 */
public interface AnywhereLocationEvent extends LocationEvent, ConstrainedEvent {

    boolean anywhere();

    static boolean anywhere(List<EventConstraint> constraints) {
        if (constraints.isEmpty()) {
            return false;
        }
        if (constraints.size() == 1 && constraints.getFirst() == ANYWHERE_EVEN_IF_DAMAGED) {
            return true;
        }
        throw new FrakCallTheAdmiralException("A location action only takes ANYWHERE_EVEN_IF_DAMAGED: " + constraints);
    }
}
