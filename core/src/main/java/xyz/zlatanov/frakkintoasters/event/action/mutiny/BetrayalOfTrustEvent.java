package xyz.zlatanov.frakkintoasters.event.action.mutiny;

import xyz.zlatanov.frakkintoasters.event.action.MutinyCardEvent;

// Betrayal of Trust (Mutiny Card)
// Action: draw 2 Treachery Cards, then look at the top Destination Card and put it back on top or at the bottom.
// Discard it.
public record BetrayalOfTrustEvent(int playerNumber) implements MutinyCardEvent {
}
