package xyz.zlatanov.frakkintoasters.event.action;

import xyz.zlatanov.frakkintoasters.event.ActionEvent;
import xyz.zlatanov.frakkintoasters.state.ship.ShipType;

public record CylonFleetEvent(int playerNumber, ShipType typeToActivate) implements ActionEvent {
}
