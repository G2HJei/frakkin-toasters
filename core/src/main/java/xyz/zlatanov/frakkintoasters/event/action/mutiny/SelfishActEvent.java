package xyz.zlatanov.frakkintoasters.event.action.mutiny;

import xyz.zlatanov.frakkintoasters.event.action.MutinyCardEvent;

// Selfish Act (Mutiny Card)
// Action: draw 2 Skill Cards. Discard it, then draw a Mutiny Card.
public record SelfishActEvent(int playerNumber) implements MutinyCardEvent {
}
