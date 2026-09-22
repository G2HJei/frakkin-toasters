package xyz.zlatanov.frakkintoasters.event.location.galactica;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;

public record WeaponsControlEvent(int playerNumber, int cylonShipId) implements PlayerEvent {
}
