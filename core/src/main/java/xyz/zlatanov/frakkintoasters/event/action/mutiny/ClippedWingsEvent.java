package xyz.zlatanov.frakkintoasters.event.action.mutiny;

import xyz.zlatanov.frakkintoasters.event.action.MutinyCardEvent;

// Clipped Wings (Mutiny Card)
// Action: every viper in space goes back to the "Reserves" and every damaged viper is repaired, then draw 2 Treachery
// Cards. Discard it. Not without a viper in space.
public record ClippedWingsEvent(int playerNumber) implements MutinyCardEvent {
}
