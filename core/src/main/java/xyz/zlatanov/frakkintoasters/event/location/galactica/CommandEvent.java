package xyz.zlatanov.frakkintoasters.event.location.galactica;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;

public record CommandEvent(int playerNumber) implements PlayerEvent {
}
