package xyz.zlatanov.frakkintoasters.event;

import java.util.Set;

// Destroys raiders in space, e.g. with "Main Batteries" or "Best of the Best".
public record DestroyRaidersEvent(Set<Integer> shipIds) implements Event {
}
