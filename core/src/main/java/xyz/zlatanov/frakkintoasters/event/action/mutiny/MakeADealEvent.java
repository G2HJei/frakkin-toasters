package xyz.zlatanov.frakkintoasters.event.action.mutiny;

import xyz.zlatanov.frakkintoasters.event.action.MutinyCardEvent;
import xyz.zlatanov.frakkintoasters.state.board.Location;

// Make a Deal (Mutiny Card)
// Action: a character in the "Brig" moves to a Galactica location. Discard it, then a chosen player draws a Mutiny Card.
// Not with nobody in the "Brig".
public record MakeADealEvent(int playerNumber, int targetPlayerNumber, Location destination, int drawerPlayerNumber) implements MutinyCardEvent {
}
