package xyz.zlatanov.frakkintoasters.event.crisis;

import xyz.zlatanov.frakkintoasters.event.Event;

// "After the Activate Cylon Ships step, return to the Resolve Crisis step (Draw a new Crisis Card and resolve it.)"
public record ReturnToCrisisStepEvent() implements Event {
}
