package xyz.zlatanov.frakkintoasters.event.officials;

import xyz.zlatanov.frakkintoasters.event.Event;

public record DetermineNextAdmiralEvent(Integer excludedPlayerNumber) implements Event {

    public DetermineNextAdmiralEvent() {
        this(null);
    }
}
