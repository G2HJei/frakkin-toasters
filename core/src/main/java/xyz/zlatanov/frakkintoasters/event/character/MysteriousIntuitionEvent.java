package xyz.zlatanov.frakkintoasters.event.character;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;

// Sharon "Boomer" Valerii's "Mysterious Intuition" (once per game): before a Crisis Card's skill check is made, she
// decides that it passes or fails instead.
public record MysteriousIntuitionEvent(int playerNumber, boolean pass) implements PlayerEvent {
}
