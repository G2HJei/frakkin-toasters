package xyz.zlatanov.frakkintoasters.event.skillcheck.ability;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;

public record PersonalVicesEvent(int playerNumber) implements PlayerEvent {
}
