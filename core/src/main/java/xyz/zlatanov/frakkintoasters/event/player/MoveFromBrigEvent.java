package xyz.zlatanov.frakkintoasters.event.player;

import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.state.board.Location;

// A card moves the character of targetPlayerNumber from the "Brig" to a location, e.g. "The President may choose 1 player
// to move from the "Brig" to "Command."" Who decides is up to the wrapping decision. Unlike LeaveBrigEvent, it isn't the
// player who leaves who acts.
public record MoveFromBrigEvent(int targetPlayerNumber, Location destination) implements Event {
}
