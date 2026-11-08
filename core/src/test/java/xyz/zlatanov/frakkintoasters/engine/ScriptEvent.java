package xyz.zlatanov.frakkintoasters.engine;

import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;

// The first event of an engine test: starts the game and queues a script of real events. It lives in the tests, so the
// processor registry test doesn't see it.
public record ScriptEvent(Followup script) implements Event {
}
