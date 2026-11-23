package xyz.zlatanov.frakkintoasters.event.action.character;

import xyz.zlatanov.frakkintoasters.event.Source;
import xyz.zlatanov.frakkintoasters.event.action.CharacterActionEvent;

import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.DECLARE_MARTIAL_LAW;

// Saul Tigh's "Declare Martial Law", once per game: the Admiral becomes President.
@Source(ability = DECLARE_MARTIAL_LAW)
public record DeclareMartialLawEvent(int playerNumber) implements CharacterActionEvent {
}
