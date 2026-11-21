package xyz.zlatanov.frakkintoasters.event.crisis;

import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.state.board.Location;

import java.util.List;

// The raiders of the activation that are in the space area activate one by one, then the other areas follow.
public record ActivateRaiderAreaEvent(Location area, List<Integer> raiderShipIds) implements Event {
}
