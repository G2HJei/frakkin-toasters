package xyz.zlatanov.frakkintoasters.event.action.mutiny;

import xyz.zlatanov.frakkintoasters.event.action.MutinyCardEvent;

// Weapons Armed (Mutiny Card)
// Action: a raptor is destroyed to gain an assault raptor, then each basestar launches 2 raiders. Not without a raptor
// in the "Reserves". The card prints no fate, it is discarded like the others.
public record WeaponsArmedEvent(int playerNumber) implements MutinyCardEvent {
}
