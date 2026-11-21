package xyz.zlatanov.frakkintoasters.event.character;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;

// Lee Adama's "Forward Thinker": after his "Executive Order" and the moves and actions of the player he chose, he may
// activate his current location.
public record ForwardThinkerEvent(int playerNumber) implements PlayerEvent {
}
