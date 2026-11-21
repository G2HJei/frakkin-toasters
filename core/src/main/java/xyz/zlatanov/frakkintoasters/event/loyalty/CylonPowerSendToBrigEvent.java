package xyz.zlatanov.frakkintoasters.event.loyalty;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;

// "Can send a character to the Brig": a character on Galactica goes to the "Brig".
public record CylonPowerSendToBrigEvent(int playerNumber, int targetPlayerNumber) implements PlayerEvent {
}
