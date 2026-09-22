package xyz.zlatanov.frakkintoasters.event.location.galactica;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;

public record ArmoryEvent(int playerNumber, int centurionId) implements PlayerEvent {
}
