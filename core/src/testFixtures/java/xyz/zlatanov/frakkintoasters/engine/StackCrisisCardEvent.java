package xyz.zlatanov.frakkintoasters.engine;

import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard;

// Puts a Crisis Card on top of the Crisis deck, so the next Crisis step draws it. It lives in the tests, so the processor
// registry test doesn't see it.
public record StackCrisisCardEvent(CrisisCard card) implements Event {
}
