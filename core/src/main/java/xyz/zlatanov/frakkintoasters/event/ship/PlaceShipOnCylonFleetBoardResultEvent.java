package xyz.zlatanov.frakkintoasters.event.ship;

import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.state.ship.ShipType;

// Places the Cylon ship in the Cylon space area that matches the die roll (rule 11.4.1.1).
public record PlaceShipOnCylonFleetBoardResultEvent(ShipType cylonShipType) implements Event {
}
