package xyz.zlatanov.frakkintoasters.event.location.cylon;

import xyz.zlatanov.frakkintoasters.event.ActionEvent;
import xyz.zlatanov.frakkintoasters.state.ship.ShipType;

// Cylon Fleet (Cylon location, Daybreak)
// Action: Activate all Cylon ships of one type or launch 2 raiders and 1 heavy raider from each basestar.
public record CylonFleetEvent(int playerNumber, ShipType typeToActivate) implements ActionEvent {
}
