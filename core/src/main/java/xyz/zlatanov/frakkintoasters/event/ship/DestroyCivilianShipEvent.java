package xyz.zlatanov.frakkintoasters.event.ship;

import xyz.zlatanov.frakkintoasters.event.Event;

public record DestroyCivilianShipEvent(int shipId) implements Event {
}
