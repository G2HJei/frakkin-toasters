package xyz.zlatanov.frakkintoasters.event.character;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;

// William Adama's "Command Authority" (once per game): once a skill check is resolved, he takes its cards into his
// hand instead of discarding them.
public record CommandAuthorityEvent(int playerNumber) implements PlayerEvent {
}
