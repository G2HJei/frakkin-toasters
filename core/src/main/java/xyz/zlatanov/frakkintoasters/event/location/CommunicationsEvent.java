package xyz.zlatanov.frakkintoasters.event.location;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;

public record CommunicationsEvent(int playerNumber, Integer civilianShipId1,
                                  Integer civilianShipId2) implements PlayerEvent {
}
