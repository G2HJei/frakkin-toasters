package xyz.zlatanov.frakkintoasters.event.location.cylon;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;

public record HumanFleetLookAtTopDestinationCardEvent(int playerNumber) implements PlayerEvent {
}
