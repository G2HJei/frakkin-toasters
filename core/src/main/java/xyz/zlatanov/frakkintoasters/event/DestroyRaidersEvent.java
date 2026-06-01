package xyz.zlatanov.frakkintoasters.event;

import java.util.Set;

public record DestroyRaidersEvent(Set<Integer> shipIds) implements Event {
    //todo
}