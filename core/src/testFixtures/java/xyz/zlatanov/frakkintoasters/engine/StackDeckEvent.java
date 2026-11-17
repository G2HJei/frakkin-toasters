package xyz.zlatanov.frakkintoasters.engine;

import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.state.deck.DeckId;

import java.util.List;

// Puts chosen cards on top of a deck, the first on top, so the next draws give them. The cards of a deck that already
// holds them are there twice. It lives in the tests, so the processor registry test doesn't see it.
public record StackDeckEvent(DeckId deck, List<?> topFirst) implements Event {
}
