package xyz.zlatanov.frakkintoasters.event.action;

import xyz.zlatanov.frakkintoasters.event.Event;

// The current player acts again, after the players an "Executive Order" or a "State of Emergency" let act.
public record ResetActingPlayerEvent() implements Event {
}
