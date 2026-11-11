package xyz.zlatanov.frakkintoasters.event.location.pegasus;

import xyz.zlatanov.frakkintoasters.event.LocationEvent;
import xyz.zlatanov.frakkintoasters.state.board.Location;

import static xyz.zlatanov.frakkintoasters.state.board.Location.PEGASUS_CIC;

// Pegasus CIC (Pegasus location)
// Action: Choose a basestar and roll a die:
// 1-3: Damage Pegasus
// 4-6: Damage basestar
// 7-8: Damage basestar twice
public record PegasusCicEvent(int playerNumber, int basestarId) implements LocationEvent {

    @Override
    public Location location() {
        return PEGASUS_CIC;
    }
}
