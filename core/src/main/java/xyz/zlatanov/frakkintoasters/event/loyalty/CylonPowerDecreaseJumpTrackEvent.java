package xyz.zlatanov.frakkintoasters.event.loyalty;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;

// "Can decrease the jump track": the Jump Preparation track moves back 2.
public record CylonPowerDecreaseJumpTrackEvent(int playerNumber) implements PlayerEvent {
}
