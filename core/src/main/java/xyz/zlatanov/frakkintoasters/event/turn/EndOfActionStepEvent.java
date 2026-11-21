package xyz.zlatanov.frakkintoasters.event.turn;

import xyz.zlatanov.frakkintoasters.event.Event;

// The current player's Action step is over. Queued only for an ability that acts then: Louanne "Kat" Katraine's "Stim
// Junkie".
public record EndOfActionStepEvent() implements Event {
}
