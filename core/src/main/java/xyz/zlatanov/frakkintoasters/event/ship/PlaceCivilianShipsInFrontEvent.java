package xyz.zlatanov.frakkintoasters.event.ship;

import xyz.zlatanov.frakkintoasters.event.Event;

// Place civilian ships in front of Galactica, e.g. "Place 1 civilian ship in front of Galactica." ("Binary Star")
public record PlaceCivilianShipsInFrontEvent(int count) implements Event {
}
