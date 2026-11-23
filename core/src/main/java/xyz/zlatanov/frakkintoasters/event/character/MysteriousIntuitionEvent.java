package xyz.zlatanov.frakkintoasters.event.character;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.event.Source;

import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.MYSTERIOUS_INTUITION;

// Sharon "Boomer" Valerii's "Mysterious Intuition" (once per game): before a Crisis Card's skill check is made, she
// decides that it passes or fails instead.
@Source(ability = MYSTERIOUS_INTUITION)
public record MysteriousIntuitionEvent(int playerNumber, boolean pass) implements PlayerEvent {
}
