package xyz.zlatanov.frakkintoasters.event.turn;

import xyz.zlatanov.frakkintoasters.event.Event;

// Every player over the hand limit discards down to it (rule 8.8).
public record DiscardStepEvent() implements Event {
}
