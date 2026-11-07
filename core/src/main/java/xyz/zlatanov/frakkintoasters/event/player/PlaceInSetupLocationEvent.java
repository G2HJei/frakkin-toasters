package xyz.zlatanov.frakkintoasters.event.player;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.state.board.Location;

// The player's character enters the board on one of the setup locations of its character sheet. Not a MoveEvent: the
// character comes from nowhere, so the movement rules (adjacency, discarding to change ships) don't apply.
public record PlaceInSetupLocationEvent(int playerNumber, Location location) implements PlayerEvent {
}
