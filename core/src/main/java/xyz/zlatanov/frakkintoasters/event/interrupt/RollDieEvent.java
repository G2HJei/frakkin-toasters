package xyz.zlatanov.frakkintoasters.event.interrupt;

import xyz.zlatanov.frakkintoasters.event.Event;

// The current player rolls the die of the roll the window before it started, or rerolls it. Queue it through
// FollowupUtils.rollDie, so the windows around it open.
public record RollDieEvent() implements Event {
}
