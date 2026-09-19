package xyz.zlatanov.frakkintoasters.event.location;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;

public record WeaponsControlEvent(int playerNumber, int cylonShipId) implements PlayerEvent {
}
