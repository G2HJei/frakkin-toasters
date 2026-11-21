package xyz.zlatanov.frakkintoasters.event.action.character;

import xyz.zlatanov.frakkintoasters.event.action.CharacterActionEvent;

// Lee "Apollo" Adama's "CAG", once per game: he activates up to 6 unmanned vipers (UnmannedViperRoundEvent).
public record CagAbilityEvent(int playerNumber) implements CharacterActionEvent {
}
