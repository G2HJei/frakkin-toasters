package xyz.zlatanov.frakkintoasters.event.action.mutiny;

import xyz.zlatanov.frakkintoasters.event.action.MutinyCardEvent;

// Bait and Switch (Mutiny Card)
// Action: draw 2 Skill Cards of any types, then shuffle 2 Treachery Cards into the Destiny deck. Discard it.
public record BaitAndSwitchEvent(int playerNumber) implements MutinyCardEvent {
}
