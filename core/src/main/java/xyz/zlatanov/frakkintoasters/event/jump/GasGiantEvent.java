package xyz.zlatanov.frakkintoasters.event.jump;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;

// "Gas Giant": "The Admiral may destroy 1 viper to gain 1 fuel."
public record GasGiantEvent(int playerNumber) implements PlayerEvent {
}
