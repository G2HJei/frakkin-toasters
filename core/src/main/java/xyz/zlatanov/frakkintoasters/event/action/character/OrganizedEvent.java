package xyz.zlatanov.frakkintoasters.event.action.character;

import xyz.zlatanov.frakkintoasters.event.Source;
import xyz.zlatanov.frakkintoasters.event.action.CharacterActionEvent;

import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.ORGANIZED;

// Louis Hoshi's "Organized", once per game, outside the "Brig": he takes the actions of 3 different undamaged
// locations, none of them Cylon, wherever he is (OrganizedLocationEvent).
@Source(ability = ORGANIZED)
public record OrganizedEvent(int playerNumber) implements CharacterActionEvent {
}
