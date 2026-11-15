package xyz.zlatanov.frakkintoasters.event.ship;

import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.state.board.Location;

// Destroy all Cylon ships in a space area of the main game board, e.g. "Choose 1 space area on the main game board and
// remove all Cylon ships in that area."
public record DestroyCylonShipsEvent(Location area) implements Event {
}
