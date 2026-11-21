package xyz.zlatanov.frakkintoasters.event.loyalty;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;

// "Can reduce morale": lose 1 morale.
public record CylonPowerReduceMoraleEvent(int playerNumber) implements PlayerEvent {
}
