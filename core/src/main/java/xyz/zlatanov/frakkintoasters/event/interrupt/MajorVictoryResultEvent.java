package xyz.zlatanov.frakkintoasters.event.interrupt;

import xyz.zlatanov.frakkintoasters.event.Event;

// Major Victory - color: LEADERSHIP
// Play after you destroy a basestar or a centurion on the Boarding Party track. Roll a die. If 5 or higher, gain 1
// morale. Limit of 1 "Major Victory" card used per turn.
// The result of the roll.
public record MajorVictoryResultEvent() implements Event {
}
