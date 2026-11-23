package xyz.zlatanov.frakkintoasters.event.action.character;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.event.Source;

import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.CAG;

// One more unmanned viper activation of "CAG", or a stop.
@Source(ability = CAG)
public record UnmannedViperRoundEvent(int playerNumber, int activationsLeft) implements PlayerEvent {
}
