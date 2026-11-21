package xyz.zlatanov.frakkintoasters.event.action.mutiny;

import xyz.zlatanov.frakkintoasters.event.action.MutinyCardEvent;

// The Strong Survive (Mutiny Card)
// Action: draw a civilian ship and destroy it, then the jump preparation track moves up 1. The card leaves the game.
public record TheStrongSurviveEvent(int playerNumber) implements MutinyCardEvent {
}
