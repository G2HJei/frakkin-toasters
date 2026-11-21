package xyz.zlatanov.frakkintoasters.event.action.character;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.state.board.Location;

import java.util.List;

// After one location of "Organized": the next one, from the locations he can act at then.
public record OrganizedNextEvent(int playerNumber, List<Location> used) implements PlayerEvent {
}
