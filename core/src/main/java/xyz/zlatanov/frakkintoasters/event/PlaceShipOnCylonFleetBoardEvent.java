package xyz.zlatanov.frakkintoasters.event;

import xyz.zlatanov.frakkintoasters.state.ship.ShipType;

public record PlaceShipOnCylonFleetBoardEvent(ShipType cylonShipType) implements Event {
}
