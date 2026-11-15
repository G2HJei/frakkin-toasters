package xyz.zlatanov.frakkintoasters.event.skillcheck.ability;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.event.ViperActivation;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.ship.ShipType;

// Protect the Fleet - color: PILOTING, values: 0
// Skill Check: If at least 3 strength in piloting Skill Cards are in this skill check, the current player may activate
// one unmanned viper.
// A null activation declines.
public record ProtectTheFleetEvent(int playerNumber, ViperActivation activation) implements PlayerEvent {

    public static ProtectTheFleetEvent decline(int playerNumber) {
        return new ProtectTheFleetEvent(playerNumber, null);
    }

    public static ProtectTheFleetEvent launch(int playerNumber, ShipType shipType, Location launchLocation) {
        return new ProtectTheFleetEvent(playerNumber, ViperActivation.launch(shipType, launchLocation));
    }

    public static ProtectTheFleetEvent move(int playerNumber, int humanFighterId, Location moveLocation) {
        return new ProtectTheFleetEvent(playerNumber, ViperActivation.move(humanFighterId, moveLocation));
    }

    public static ProtectTheFleetEvent attack(int playerNumber, int humanFighterId, int attackCylonShipId) {
        return new ProtectTheFleetEvent(playerNumber, ViperActivation.attack(humanFighterId, attackCylonShipId));
    }

    public static ProtectTheFleetEvent escort(int playerNumber, int humanFighterId, int escortCivilianShipId) {
        return new ProtectTheFleetEvent(playerNumber, ViperActivation.escort(humanFighterId, escortCivilianShipId));
    }
}
