package xyz.zlatanov.frakkintoasters.event.deck;

import xyz.zlatanov.frakkintoasters.event.Event;

// Shuffle Treachery Cards into the Destiny deck, e.g. "Shuffle 2 Treachery Cards into the Destiny deck."
public record ShuffleTreacheryIntoDestinyDeckEvent(int count) implements Event {
}
