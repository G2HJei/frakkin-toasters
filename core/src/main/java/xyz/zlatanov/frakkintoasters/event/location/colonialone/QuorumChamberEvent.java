package xyz.zlatanov.frakkintoasters.event.location.colonialone;

import xyz.zlatanov.frakkintoasters.event.LocationEvent;
import xyz.zlatanov.frakkintoasters.state.board.Location;

import static xyz.zlatanov.frakkintoasters.state.board.Location.QUORUM_CHAMBER;

// Quorum Chamber (Colonial One location, Daybreak)
// Action: If you are the President, draw 1 Quorum Card. You may then either draw 1 additional Quorum Card or play 1
// from your hand.
public record QuorumChamberEvent(int playerNumber) implements LocationEvent {

    @Override
    public Location location() {
        return QUORUM_CHAMBER;
    }
}
