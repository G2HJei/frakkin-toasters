package xyz.zlatanov.frakkintoasters.event.player;

import xyz.zlatanov.frakkintoasters.event.LocationEvent;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.character.Character;
import xyz.zlatanov.frakkintoasters.state.ship.ShipType;

public record LaunchViperEvent(ShipType shipType, Location location, Character pilot,
                               Integer viperToLand) implements LocationEvent {
}
