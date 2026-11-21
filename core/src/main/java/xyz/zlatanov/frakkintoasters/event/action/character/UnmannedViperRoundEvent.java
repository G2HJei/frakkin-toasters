package xyz.zlatanov.frakkintoasters.event.action.character;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;

// One more unmanned viper activation of "CAG", or a stop.
public record UnmannedViperRoundEvent(int playerNumber, int activationsLeft) implements PlayerEvent {
}
