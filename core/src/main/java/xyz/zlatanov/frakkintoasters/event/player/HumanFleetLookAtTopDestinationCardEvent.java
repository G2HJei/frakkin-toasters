package xyz.zlatanov.frakkintoasters.event.player;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;

public record HumanFleetLookAtTopDestinationCardEvent(int playerNumber) implements PlayerEvent {
}
