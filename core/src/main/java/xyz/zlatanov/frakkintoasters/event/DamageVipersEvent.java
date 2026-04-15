package xyz.zlatanov.frakkintoasters.event;

import java.util.List;

public record DamageVipersEvent(List<Integer> shipIds) implements Event {
}
