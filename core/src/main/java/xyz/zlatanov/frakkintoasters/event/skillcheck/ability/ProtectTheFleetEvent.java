package xyz.zlatanov.frakkintoasters.event.skillcheck.ability;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.ship.ShipType;

public record ProtectTheFleetEvent(int playerNumber,

                                   ShipType launchShipType,
                                   Location launchLocation,

                                   Integer humanFighterId,
                                   Location moveLocation,
                                   Integer attackCylonShipId,
                                   Integer escortCivilianShipId) implements PlayerEvent {

    public static ProtectTheFleetEvent launch(int playerNumber, ShipType shipType, Location launchLocation) {
        return new ProtectTheFleetEvent(playerNumber, shipType, launchLocation, null, null, null, null);
    }

    public static ProtectTheFleetEvent move(int playerNumber, int humanFighterId, Location moveLocation) {
        return new ProtectTheFleetEvent(playerNumber, null, null, humanFighterId, moveLocation, null, null);
    }

    public static ProtectTheFleetEvent attack(int playerNumber, int humanFighterId, int attackCylonShipId) {
        return new ProtectTheFleetEvent(playerNumber, null, null, humanFighterId, null, attackCylonShipId, null);
    }

    public static ProtectTheFleetEvent escort(int playerNumber, int escortCivilianShipId) {
        return new ProtectTheFleetEvent(playerNumber, null, null, null, null, null, escortCivilianShipId);
    }
}
