package xyz.zlatanov.frakkintoasters.event.action.character;

import xyz.zlatanov.frakkintoasters.event.action.CharacterActionEvent;
import xyz.zlatanov.frakkintoasters.state.board.Location;

// Sharon "Athena" Agathon's "Resolute", once per game: she takes the action of an undamaged location, wherever
// she is.
public record ResoluteEvent(int playerNumber, Location location) implements CharacterActionEvent {
}
