package xyz.zlatanov.frakkintoasters.event.action.mutiny;

import xyz.zlatanov.frakkintoasters.event.action.MutinyCardEvent;

// Armed Resistance (Mutiny Card)
// Action: the Admiral goes to "Sickbay", then the player looks at the top Crisis Card and puts it back on top or
// at the bottom. Discard it.
public record ArmedResistanceEvent(int playerNumber) implements MutinyCardEvent {
}
