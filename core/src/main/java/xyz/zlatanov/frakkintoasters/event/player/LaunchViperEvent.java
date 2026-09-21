package xyz.zlatanov.frakkintoasters.event.player;

import xyz.zlatanov.frakkintoasters.event.LocationEvent;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.character.Character;
import xyz.zlatanov.frakkintoasters.state.ship.ShipType;

public record LaunchViperEvent(ShipType shipType, Location location, Character pilot,
                               Integer unmannedViperId, boolean mustLaunchInViper) implements LocationEvent {
    public LaunchViperEvent(ShipType shipType, Location location) {
        this(shipType, location, null, null, false);
    }

    public LaunchViperEvent(ShipType shipType, Location location, Character character, Integer unmannedViperId) {
        this(shipType, location, character, unmannedViperId, false);
    }
}
