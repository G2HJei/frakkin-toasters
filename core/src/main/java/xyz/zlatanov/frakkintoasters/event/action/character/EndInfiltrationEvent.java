package xyz.zlatanov.frakkintoasters.event.action.character;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;

// An ability ends the player's Infiltration at the "Resurrection Ship", e.g. "Visions", unless something ended
// it first.
public record EndInfiltrationEvent(int playerNumber) implements PlayerEvent {
}
