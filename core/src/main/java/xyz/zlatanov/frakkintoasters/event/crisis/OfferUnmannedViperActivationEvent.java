package xyz.zlatanov.frakkintoasters.event.crisis;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;

// "The CAG may then activate 1 unmanned viper", offered as the board is when the card gets there: its effects before
// it may have damaged the vipers it would activate.
public record OfferUnmannedViperActivationEvent(int playerNumber) implements PlayerEvent {
}
