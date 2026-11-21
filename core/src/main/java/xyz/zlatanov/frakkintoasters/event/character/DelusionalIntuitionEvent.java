package xyz.zlatanov.frakkintoasters.event.character;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;

// Gaius Baltar's "Delusional Intuition": after he draws a Crisis Card, he draws 1 Skill Card from any deck.
public record DelusionalIntuitionEvent(int playerNumber) implements PlayerEvent {
}
