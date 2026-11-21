package xyz.zlatanov.frakkintoasters.event.action.mutiny;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;

// "Set the Agenda": the player looks at the President's Quorum Cards, then chooses the ones that go to the bottom.
public record SetTheAgendaLookEvent(int playerNumber) implements PlayerEvent {
}
