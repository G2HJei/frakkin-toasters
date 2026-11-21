package xyz.zlatanov.frakkintoasters.event.action.mutiny;

import xyz.zlatanov.frakkintoasters.event.action.MutinyCardEvent;

// Set the Agenda (Mutiny Card)
// Action: the President draws 2 Quorum Cards, then the player looks at the President's Quorum Cards and puts 2 of
// them at the bottom of the deck, in the order he likes. Discard it. Not as President.
public record SetTheAgendaEvent(int playerNumber) implements MutinyCardEvent {
}
