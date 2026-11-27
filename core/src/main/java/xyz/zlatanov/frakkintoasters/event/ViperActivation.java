package xyz.zlatanov.frakkintoasters.event;

import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.ship.ShipType;

// One activation of a viper (rule 11.1.1): launch one from the "Reserves", or move, attack or escort a civilian ship with
// one in space. The fields of the other options are null.
public record ViperActivation(ShipType launchShipType,
                              Location launchLocation,

                              Integer humanFighterId,
                              Location moveLocation,
                              Integer attackCylonShipId,
                              Integer escortCivilianShipId) {

    public static ViperActivation launch(ShipType shipType, Location launchLocation) {
        return new ViperActivation(shipType, launchLocation, null, null, null, null);
    }

    public static ViperActivation move(int humanFighterId, Location moveLocation) {
        return new ViperActivation(null, null, humanFighterId, moveLocation, null, null);
    }

    public static ViperActivation attack(int humanFighterId, int attackCylonShipId) {
        return new ViperActivation(null, null, humanFighterId, null, attackCylonShipId, null);
    }

    public static ViperActivation escort(int humanFighterId, int escortCivilianShipId) {
        return new ViperActivation(null, null, humanFighterId, null, null, escortCivilianShipId);
    }
}
