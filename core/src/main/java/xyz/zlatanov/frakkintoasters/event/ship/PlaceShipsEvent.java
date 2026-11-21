package xyz.zlatanov.frakkintoasters.event.ship;

import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.state.board.Location;

// Place basestars and raiders in front of Galactica and civilian ships behind it, e.g. "Place a basestar and 3 raiders
// in front of Galactica and 2 civilian ships behind Galactica."
//
// @param cylonArea where the basestars and raiders go instead of in front of Galactica, e.g. the space area a "Necessary
//                  Risk" chooses; null for in front of Galactica
public record PlaceShipsEvent(int basestars, int raiders, int civilianShips, Location cylonArea) implements Event {

    public PlaceShipsEvent(int basestars, int raiders, int civilianShips) {
        this(basestars, raiders, civilianShips, null);
    }
}
