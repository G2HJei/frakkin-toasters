package xyz.zlatanov.frakkintoasters.event.crisis;

import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.state.board.Location;

import java.util.List;

public record ActivateRaiderAreaEvent(Location area, List<Integer> raiderShipIds) implements Event {
}
