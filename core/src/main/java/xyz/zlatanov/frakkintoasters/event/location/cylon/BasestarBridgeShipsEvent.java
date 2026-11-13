package xyz.zlatanov.frakkintoasters.event.location.cylon;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.ship.ShipType;

// A "Basestar Bridge" ability: "Place 1 Basestar or 3 Raiders in any 1 area on this game board." (the Cylon Fleet board)
public record BasestarBridgeShipsEvent(int playerNumber, ShipType shipType, Location location) implements PlayerEvent {
}
