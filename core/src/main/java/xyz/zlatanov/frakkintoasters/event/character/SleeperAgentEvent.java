package xyz.zlatanov.frakkintoasters.event.character;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;

// Sharon "Boomer" Valerii's "Sleeper Agent": after her two Loyalty Cards of the Sleeper Agent phase, she moves to the
// "Brig".
public record SleeperAgentEvent(int playerNumber) implements PlayerEvent {
}
