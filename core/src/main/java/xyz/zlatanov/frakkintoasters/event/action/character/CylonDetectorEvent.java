package xyz.zlatanov.frakkintoasters.event.action.character;

import xyz.zlatanov.frakkintoasters.event.Source;
import xyz.zlatanov.frakkintoasters.event.action.CharacterActionEvent;

import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.CYLON_DETECTOR;

// Gaius Baltar's "Cylon Detector", once per game: he looks at every Loyalty Card of another player.
@Source(ability = CYLON_DETECTOR)
public record CylonDetectorEvent(int playerNumber, int targetPlayerNumber) implements CharacterActionEvent {
}
