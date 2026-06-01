package xyz.zlatanov.frakkintoasters.event.player;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.state.character.Character;

public record SelectCharacterEvent(int playerNumber, Character selectedCharacter) implements PlayerEvent {
}
