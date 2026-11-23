package xyz.zlatanov.frakkintoasters.event.character;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.event.Source;

import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.DELUSIONAL_INTUITION;

// Gaius Baltar's "Delusional Intuition": after he draws a Crisis Card, he draws 1 Skill Card from any deck.
@Source(ability = DELUSIONAL_INTUITION)
public record DelusionalIntuitionEvent(int playerNumber) implements PlayerEvent {
}
