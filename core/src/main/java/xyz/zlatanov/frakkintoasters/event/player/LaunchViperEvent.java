package xyz.zlatanov.frakkintoasters.event.player;

import xyz.zlatanov.frakkintoasters.event.LocationEvent;
import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.event.constraint.ConstrainedEvent;
import xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.character.Character;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;
import xyz.zlatanov.frakkintoasters.state.ship.ShipType;

import java.util.List;

import static xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint.MUST_LAUNCH_IN_VIPER;

// The player launches a viper, unmanned or piloted by his own character. Supports the MUST_LAUNCH_IN_VIPER
// constraint, as the mustLaunchInViper flag.
public record LaunchViperEvent(int playerNumber, ShipType shipType, Location location, Character pilot,
                               Integer unmannedViperId,
                               boolean mustLaunchInViper) implements PlayerEvent, LocationEvent, ConstrainedEvent {
    public LaunchViperEvent(int playerNumber, ShipType shipType, Location location) {
        this(playerNumber, shipType, location, null, null, false);
    }

    public LaunchViperEvent(int playerNumber, ShipType shipType, Location location, Character pilot,
                            Integer unmannedViperId) {
        this(playerNumber, shipType, location, pilot, unmannedViperId, false);
    }

    @Override
    public LaunchViperEvent withConstraints(List<EventConstraint> constraints) {
        if (constraints.isEmpty() || constraints.size() == 1 && constraints.getFirst() == MUST_LAUNCH_IN_VIPER) {
            return new LaunchViperEvent(playerNumber, shipType, location, pilot, unmannedViperId,
                    constraints.contains(MUST_LAUNCH_IN_VIPER));
        }
        throw new FrakCallTheAdmiralException("A viper launch only takes MUST_LAUNCH_IN_VIPER: " + constraints);
    }
}
