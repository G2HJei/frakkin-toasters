package xyz.zlatanov.frakkintoasters.event.action.character;

import xyz.zlatanov.frakkintoasters.event.action.CharacterMovementEvent;

// Cavil's "Aggressive Tactics" (Movement, once per game): instead of moving he takes two actions. His Action step
// still follows.
public record AggressiveTacticsEvent(int playerNumber) implements CharacterMovementEvent {
}
