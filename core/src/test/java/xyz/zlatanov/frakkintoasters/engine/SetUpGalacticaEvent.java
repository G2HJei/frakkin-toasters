package xyz.zlatanov.frakkintoasters.engine;

import xyz.zlatanov.frakkintoasters.event.Event;

// Places the starting ships, as StartGameEvent does. It lives in the tests, so the processor registry test doesn't see it.
public record SetUpGalacticaEvent() implements Event {
}
