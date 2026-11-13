package xyz.zlatanov.frakkintoasters.event.location.cylon;

import xyz.zlatanov.frakkintoasters.event.LocationEvent;
import xyz.zlatanov.frakkintoasters.state.board.Location;

import static xyz.zlatanov.frakkintoasters.state.board.Location.BASESTAR_BRIDGE;

// Basestar Bridge (Cylon Fleet board location, Exodus)
// Action: Choose 2 of the following abilities (see BasestarBridgeAbility). The player names the first ability, and
// chooses the second once the first is resolved (rule 9.7).
public record BasestarBridgeEvent(int playerNumber, BasestarBridgeAbility firstAbility) implements LocationEvent {

    @Override
    public Location location() {
        return BASESTAR_BRIDGE;
    }
}
