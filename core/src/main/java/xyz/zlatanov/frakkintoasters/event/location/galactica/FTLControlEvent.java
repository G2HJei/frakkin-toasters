package xyz.zlatanov.frakkintoasters.event.location.galactica;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;

public record FTLControlEvent(int playerNumber) implements PlayerEvent {
}
