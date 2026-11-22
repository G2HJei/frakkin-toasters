package xyz.zlatanov.frakkintoasters.event.crisis;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;

// "The CAG may then activate 1 unmanned viper", or the second of "Command", offered as the board is when it gets
// there: the effects before it may have damaged the vipers it would activate, or "Alert Viper Pilot" boarded one.
public record OfferUnmannedViperActivationEvent(int playerNumber) implements PlayerEvent {
}
