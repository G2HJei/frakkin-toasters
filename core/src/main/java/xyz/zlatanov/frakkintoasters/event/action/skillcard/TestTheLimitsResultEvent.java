package xyz.zlatanov.frakkintoasters.event.action.skillcard;

import xyz.zlatanov.frakkintoasters.event.Event;

// What the die decides after a "Test the Limits": on 5 or lower, Galactica is damaged.
public record TestTheLimitsResultEvent() implements Event {
}
