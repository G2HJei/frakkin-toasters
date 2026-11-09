package xyz.zlatanov.frakkintoasters.event.turn;

import xyz.zlatanov.frakkintoasters.event.Event;

// The fleet prepares for jump if the Crisis Card has the "prepare for jump" icon (rule 8.7).
public record PrepareForJumpStepEvent() implements Event {
}
