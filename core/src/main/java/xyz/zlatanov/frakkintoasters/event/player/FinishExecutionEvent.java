package xyz.zlatanov.frakkintoasters.event.player;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.state.character.Character;

// Ends the execution of a human character once his player has chosen a new one: the player gets a new Loyalty Card, and
// the titles the executed character held change hands (rules 7.1, 7.3).
public record FinishExecutionEvent(int playerNumber, Character executedCharacter) implements PlayerEvent {
}
