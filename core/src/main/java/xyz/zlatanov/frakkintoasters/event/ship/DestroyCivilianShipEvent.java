package xyz.zlatanov.frakkintoasters.event.ship;

import xyz.zlatanov.frakkintoasters.event.Event;

// A civilian ship in a space area is destroyed. Brendan "Hot Dog" Costanza may "Escort" it first, unless escortOffered.
public record DestroyCivilianShipEvent(int shipId, boolean escortOffered) implements Event {

    public DestroyCivilianShipEvent(int shipId) {
        this(shipId, false);
    }
}
