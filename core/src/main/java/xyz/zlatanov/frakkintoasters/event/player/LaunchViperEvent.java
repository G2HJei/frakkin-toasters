package xyz.zlatanov.frakkintoasters.event.player;

import xyz.zlatanov.frakkintoasters.event.LocationEvent;
import xyz.zlatanov.frakkintoasters.event.constraint.ConstrainedEvent;
import xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.character.Character;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;
import xyz.zlatanov.frakkintoasters.state.ship.ShipType;

import java.util.List;

import static xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint.MUST_LAUNCH_IN_VIPER;

// Supports the MUST_LAUNCH_IN_VIPER constraint, as the mustLaunchInViper flag.
public record LaunchViperEvent(ShipType shipType, Location location, Character pilot,
                               Integer unmannedViperId,
                               boolean mustLaunchInViper) implements LocationEvent, ConstrainedEvent {
    public LaunchViperEvent(ShipType shipType, Location location) {
        this(shipType, location, null, null, false);
    }

    public LaunchViperEvent(ShipType shipType, Location location, Character character, Integer unmannedViperId) {
        this(shipType, location, character, unmannedViperId, false);
    }

    @Override
    public LaunchViperEvent withConstraints(List<EventConstraint> constraints) {
        if (constraints.isEmpty() || constraints.size() == 1 && constraints.getFirst() == MUST_LAUNCH_IN_VIPER) {
            return new LaunchViperEvent(shipType, location, pilot, unmannedViperId, constraints.contains(MUST_LAUNCH_IN_VIPER));
        }
        throw new FrakCallTheAdmiralException("A viper launch only takes MUST_LAUNCH_IN_VIPER: " + constraints);
    }
}
