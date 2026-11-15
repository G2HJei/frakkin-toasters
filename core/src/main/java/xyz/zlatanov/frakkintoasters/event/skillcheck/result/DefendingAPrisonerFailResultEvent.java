package xyz.zlatanov.frakkintoasters.event.skillcheck.result;

import xyz.zlatanov.frakkintoasters.event.Event;

// Defending a Prisoner, after the die roll of the fail result: if 4 or lower, the current player is executed.
public record DefendingAPrisonerFailResultEvent() implements Event {
}
