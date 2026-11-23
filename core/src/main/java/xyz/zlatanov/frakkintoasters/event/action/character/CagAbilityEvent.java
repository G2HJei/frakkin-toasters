package xyz.zlatanov.frakkintoasters.event.action.character;

import xyz.zlatanov.frakkintoasters.event.Source;
import xyz.zlatanov.frakkintoasters.event.action.CharacterActionEvent;

import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.CAG;

// Lee "Apollo" Adama's "CAG", once per game: he activates up to 6 unmanned vipers (UnmannedViperRoundEvent).
@Source(ability = CAG)
public record CagAbilityEvent(int playerNumber) implements CharacterActionEvent {
}
