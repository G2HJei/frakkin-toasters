package xyz.zlatanov.frakkintoasters.event.action.mutiny;

import xyz.zlatanov.frakkintoasters.event.action.MutinyCardEvent;

// Ruined Reputation (Mutiny Card)
// Action: a human of the player's choice draws 2 Skill Cards of any types, then the player rolls a die: on 4 or less
// that human goes to the "Brig". Discard it.
public record RuinedReputationEvent(int playerNumber, int targetPlayerNumber) implements MutinyCardEvent {
}
