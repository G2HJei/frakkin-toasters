package xyz.zlatanov.frakkintoasters.event.action.character;

import xyz.zlatanov.frakkintoasters.event.action.CharacterActionEvent;

// Felix Gaeta's "Coup", once per game: he becomes Admiral, moving from the "Brig" to "Command" first if he is
// there.
public record CoupEvent(int playerNumber) implements CharacterActionEvent {
}
