package xyz.zlatanov.frakkintoasters.event.ship;

import xyz.zlatanov.frakkintoasters.event.Event;

// Place raiders behind Galactica, e.g. "Place 3 raiders behind Galactica." ("Cylon Raiders")
public record PlaceRaidersBehindEvent(int count) implements Event {
}
