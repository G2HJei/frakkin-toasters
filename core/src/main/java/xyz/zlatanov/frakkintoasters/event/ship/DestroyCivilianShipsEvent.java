package xyz.zlatanov.frakkintoasters.event.ship;

import xyz.zlatanov.frakkintoasters.event.Event;

// The current player chooses civilian ships on the main game board to destroy, e.g. "Destroy 2 civilian ships."
public record DestroyCivilianShipsEvent(int count) implements Event {
}
