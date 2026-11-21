package xyz.zlatanov.frakkintoasters.event.character;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;

// Lee Adama's "Executive Order" is over: he is offered "Forward Thinker" if he can still use it then. Queued only when
// he has the ability.
public record OfferForwardThinkerEvent(int playerNumber) implements PlayerEvent {
}
