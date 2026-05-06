package xyz.zlatanov.frakkintoasters.event;

import java.util.Set;

public record DamageHumanFighterEvent(Set<Integer> shipIds) implements Event {
}
