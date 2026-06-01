package xyz.zlatanov.frakkintoasters.event.player;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;

public record HumanFleetLookAtTopCrisisCardEvent(int playerNumber) implements PlayerEvent {
}
