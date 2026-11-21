package xyz.zlatanov.frakkintoasters.event.character;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.state.board.Location;

// Louis Hoshi activated the location on his turn: he is offered "Dutiful" if he can still use it once the action is done.
public record OfferDutifulEvent(int playerNumber, Location location) implements PlayerEvent {
}
