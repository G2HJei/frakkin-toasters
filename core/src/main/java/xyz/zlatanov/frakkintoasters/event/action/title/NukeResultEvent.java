package xyz.zlatanov.frakkintoasters.event.action.title;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.state.board.Location;

// What the nuke's roll does in its space area (rule 11.6.1). The player who launched it picks the basestar.
public record NukeResultEvent(int playerNumber, Location area) implements PlayerEvent {
}
