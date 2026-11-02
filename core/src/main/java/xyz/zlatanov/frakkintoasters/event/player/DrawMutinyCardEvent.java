package xyz.zlatanov.frakkintoasters.event.player;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;

// The player draws 1 Mutiny Card.
public record DrawMutinyCardEvent(int playerNumber) implements PlayerEvent {
}
