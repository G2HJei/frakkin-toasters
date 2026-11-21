package xyz.zlatanov.frakkintoasters.event.action.mutiny;

import xyz.zlatanov.frakkintoasters.event.action.MutinyCardEvent;

// Violent Protest (Mutiny Card)
// Action: draw 2 Politics Cards, and the President goes to "Sickbay" (in the "Brig" he stays, rule A2.3.3.3). Discard
// it.
public record ViolentProtestEvent(int playerNumber) implements MutinyCardEvent {
}
