package xyz.zlatanov.frakkintoasters.event.action.character;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.state.board.Location;

import java.util.List;

// The next location of "Organized", after the ones already used.
public record OrganizedLocationEvent(int playerNumber, Location location, List<Location> used) implements PlayerEvent {
}
