package xyz.zlatanov.frakkintoasters.event.character;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;

// Saul Tigh's "Cylon Hatred": whoever activates the "Admiral's Quarters", he may make its check 3 easier.
public record CylonHatredEvent(int playerNumber) implements PlayerEvent {
}
