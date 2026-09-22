package xyz.zlatanov.frakkintoasters.event.ship;

import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.state.ship.ShipType;

public record PlaceShipOnCylonFleetBoardEvent(ShipType cylonShipType) implements Event {
}
