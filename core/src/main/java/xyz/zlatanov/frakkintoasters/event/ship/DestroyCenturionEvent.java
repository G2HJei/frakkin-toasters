package xyz.zlatanov.frakkintoasters.event.ship;

import xyz.zlatanov.frakkintoasters.event.Event;

// Destroys a centurion on the Boarding Party track, e.g. after the "Armory" roll. "Major Victory" may follow.
public record DestroyCenturionEvent(int centurionId) implements Event {
}
