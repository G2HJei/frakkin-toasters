package xyz.zlatanov.frakkintoasters.event.crisis;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;

// "Return to Duty": each character on Galactica with piloting may launch himself in a viper, asked in turn as the
// "Reserves" are then: a launch before his may have taken the last viper.
public record OfferPilotLaunchEvent(int playerNumber) implements PlayerEvent {
}
