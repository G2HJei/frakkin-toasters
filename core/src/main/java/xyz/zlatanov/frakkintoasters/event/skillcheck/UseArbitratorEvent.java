package xyz.zlatanov.frakkintoasters.event.skillcheck;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;

// The Arbitrator discards "Assign Arbitrator" to raise or lower the "Admiral's Quarters" difficulty by 3.
//
// @param adjustment 3 or -3
public record UseArbitratorEvent(int playerNumber, int adjustment) implements PlayerEvent {
}
