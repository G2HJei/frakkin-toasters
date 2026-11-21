package xyz.zlatanov.frakkintoasters.event.action.character;

import xyz.zlatanov.frakkintoasters.event.action.CharacterActionEvent;

// Gaius Baltar's "Cylon Detector", once per game: he looks at every Loyalty Card of another player.
public record CylonDetectorEvent(int playerNumber, int targetPlayerNumber) implements CharacterActionEvent {
}
