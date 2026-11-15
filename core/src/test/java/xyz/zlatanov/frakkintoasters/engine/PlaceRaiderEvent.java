package xyz.zlatanov.frakkintoasters.engine;

import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.state.board.Location;

// Places a raider from the supply in a space area for an engine test.
public record PlaceRaiderEvent(Location location) implements Event {
}
