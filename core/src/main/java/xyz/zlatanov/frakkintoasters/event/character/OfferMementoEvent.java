package xyz.zlatanov.frakkintoasters.event.character;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;

// Population was just lost: Brendan "Hot Dog" Costanza is offered "Memento" if he can still use it this turn.
public record OfferMementoEvent(int playerNumber) implements PlayerEvent {
}
