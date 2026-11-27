package xyz.zlatanov.frakkintoasters.event.player;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;

public record DrawMutinyCardEvent(int playerNumber) implements PlayerEvent {
}
