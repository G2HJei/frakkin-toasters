package xyz.zlatanov.frakkintoasters.event.skillcheck.ability;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.ship.ShipType;

// Protect the Fleet - color: PILOTING, values: 0
// Skill Check: If at least 3 strength in piloting Skill Cards are in this skill check, the current player may activate
// one unmanned viper.
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

    // The viper escorts a civilian ship in its own space area.
    public static ProtectTheFleetEvent escort(int playerNumber, int humanFighterId, int escortCivilianShipId) {
        return new ProtectTheFleetEvent(playerNumber, null, null, humanFighterId, null, null, escortCivilianShipId);
    }
}
