package xyz.zlatanov.frakkintoasters.event.skillcheck.ability;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;

public record ProtectTheFleetEvent(int playerNumber, int viperId) implements PlayerEvent {
}
