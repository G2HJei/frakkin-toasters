package xyz.zlatanov.frakkintoasters.event.action.character;

import xyz.zlatanov.frakkintoasters.event.Source;
import xyz.zlatanov.frakkintoasters.event.action.CharacterMovementEvent;

import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.AGGRESSIVE_TACTICS;

// Cavil's "Aggressive Tactics" (Movement, once per game): instead of moving he takes two actions. His Action step
// still follows.
@Source(ability = AGGRESSIVE_TACTICS)
public record AggressiveTacticsEvent(int playerNumber) implements CharacterMovementEvent {
}
