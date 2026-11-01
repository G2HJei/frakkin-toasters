package xyz.zlatanov.frakkintoasters.event.officials;

import xyz.zlatanov.frakkintoasters.event.Event;

public record DetermineNextPresidentEvent(Integer excludedPlayerNumber) implements Event {

    public DetermineNextPresidentEvent() {
        this(null);
    }
}
