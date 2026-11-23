package xyz.zlatanov.frakkintoasters.event.crisis;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.event.Source;

import static xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard.RETURN_TO_DUTY;

// "Return to Duty": each character on Galactica with piloting may launch himself in a viper, asked in turn as the
// "Reserves" are then: a launch before his may have taken the last viper.
@Source(crisisCard = RETURN_TO_DUTY)
public record OfferPilotLaunchEvent(int playerNumber) implements PlayerEvent {
}
