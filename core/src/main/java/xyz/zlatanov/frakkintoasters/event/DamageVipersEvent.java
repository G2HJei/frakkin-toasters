package xyz.zlatanov.frakkintoasters.event;

import java.util.Set;

public record DamageVipersEvent(Set<Integer> shipIds) implements Event {
}
