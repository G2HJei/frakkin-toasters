package xyz.zlatanov.frakkintoasters.event.ship;

import xyz.zlatanov.frakkintoasters.event.Event;

// Destroys a heavy raider in space, e.g. with "Authorization of Brutal Force".
public record DestroyHeavyRaiderEvent(int shipId) implements Event {
}
