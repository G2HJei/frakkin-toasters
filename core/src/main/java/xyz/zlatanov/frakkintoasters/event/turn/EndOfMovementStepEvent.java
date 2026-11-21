package xyz.zlatanov.frakkintoasters.event.turn;

import xyz.zlatanov.frakkintoasters.event.Event;

// The current player's Movement step is over. Queued only for an ability that acts then: Ellen Tigh's "Politically
// Adroit" and Romo Lampkin's "Kleptomania".
public record EndOfMovementStepEvent() implements Event {
}
