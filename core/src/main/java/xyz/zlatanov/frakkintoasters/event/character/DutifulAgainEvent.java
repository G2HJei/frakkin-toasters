package xyz.zlatanov.frakkintoasters.event.character;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.state.board.Location;

// After the card "Dutiful" costs Louis Hoshi, the location's action again, if he still can take it: a "Bait" he
// discarded draws a Mutiny Card, which may have sent him to the "Brig" in between.
public record DutifulAgainEvent(int playerNumber, Location location) implements PlayerEvent {
}
