package xyz.zlatanov.frakkintoasters.event.location;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;

public record CommunicationsEvent(int playerNumber, int civilianShipId1, int civilianShipId2) implements PlayerEvent {
}
