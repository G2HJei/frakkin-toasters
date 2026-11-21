package xyz.zlatanov.frakkintoasters.event.action.mutiny;

import xyz.zlatanov.frakkintoasters.event.action.MutinyCardEvent;

// Peaceful Resistance (Mutiny Card)
// Action: the player goes to "Sickbay" (in the "Brig" he stays, rule A2.3.3.1), then rolls a die: on 4 or less the
// Admiral goes to the "Brig". Discard it.
public record PeacefulResistanceEvent(int playerNumber) implements MutinyCardEvent {
}
