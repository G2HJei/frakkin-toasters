package xyz.zlatanov.frakkintoasters.engine;

import xyz.zlatanov.frakkintoasters.event.Event;

// Loses 1 fuel, then throws: a bug in the middle of an event, after it changed the game. Test-only, like ScriptEvent.
public record FailingEvent() implements Event {
}
