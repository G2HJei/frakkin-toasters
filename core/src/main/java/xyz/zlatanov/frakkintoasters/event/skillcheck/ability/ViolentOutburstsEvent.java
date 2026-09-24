package xyz.zlatanov.frakkintoasters.event.skillcheck.ability;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;

public record ViolentOutburstsEvent(int playerNumber) implements PlayerEvent {
}
