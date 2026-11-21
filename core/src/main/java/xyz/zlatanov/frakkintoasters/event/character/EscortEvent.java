package xyz.zlatanov.frakkintoasters.event.character;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;

// Brendan "Hot Dog" Costanza's "Escort" (once per game): before a civilian ship in a space area is flipped over and
// destroyed, he destroys an undamaged viper instead, and the civilian ship is shuffled back into the unused ones.
public record EscortEvent(int playerNumber, int civilianShipId, int viperId) implements PlayerEvent {
}
