package xyz.zlatanov.frakkintoasters.event.skillcheck.result;

import xyz.zlatanov.frakkintoasters.event.Event;

// Keep Tabs on Visitor, after the die roll of the fail result: if 4 or lower, -2 population.
public record KeepTabsOnVisitorFailResultEvent() implements Event {
}
