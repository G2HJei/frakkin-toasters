package xyz.zlatanov.frakkintoasters.event.officials;

import xyz.zlatanov.frakkintoasters.event.Event;

public record DetermineNextCagEvent(Integer excludedPlayerNumber) implements Event {

    public DetermineNextCagEvent() {
        this(null);
    }
}
