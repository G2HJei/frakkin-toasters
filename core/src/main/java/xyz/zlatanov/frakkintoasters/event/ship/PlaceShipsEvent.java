package xyz.zlatanov.frakkintoasters.event.ship;

import xyz.zlatanov.frakkintoasters.event.Event;

// Place basestars and raiders in front of Galactica and civilian ships behind it, e.g. "Place a basestar and 3 raiders
// in front of Galactica and 2 civilian ships behind Galactica."
public record PlaceShipsEvent(int basestars, int raiders, int civilianShips) implements Event {
}
