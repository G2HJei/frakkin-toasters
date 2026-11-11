package xyz.zlatanov.frakkintoasters.event.location.cylon;

import xyz.zlatanov.frakkintoasters.event.LocationEvent;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.ship.ShipType;

import static xyz.zlatanov.frakkintoasters.state.board.Location.CYLON_FLEET;

// Cylon Fleet (Cylon location, Daybreak)
// Action: Activate all Cylon ships of one type or launch 2 raiders and 1 heavy raider from each basestar.
public record CylonFleetEvent(int playerNumber, ShipType typeToActivate) implements LocationEvent {

    @Override
    public Location location() {
        return CYLON_FLEET;
    }
}
