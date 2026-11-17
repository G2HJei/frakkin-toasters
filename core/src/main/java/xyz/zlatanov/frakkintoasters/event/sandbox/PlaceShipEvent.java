package xyz.zlatanov.frakkintoasters.event.sandbox;

import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.ship.ShipType;

// Places a basestar, a raider or a heavy raider from the supply, or a civilian ship from its deck, in a space area of
// Galactica, as the rules place them.
public record PlaceShipEvent(ShipType shipType, Location location) implements SandboxEvent {
}
