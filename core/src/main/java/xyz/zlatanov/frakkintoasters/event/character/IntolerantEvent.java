package xyz.zlatanov.frakkintoasters.event.character;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;

// Helena Cain's "Intolerant": when an "Admiral's Quarters" check passes with a strength of 10 or more, she may have its
// target executed instead of sent to the "Brig".
public record IntolerantEvent(int playerNumber, int targetPlayerNumber) implements PlayerEvent {
}
