package xyz.zlatanov.frakkintoasters.event.action.character;

import xyz.zlatanov.frakkintoasters.event.action.CharacterActionEvent;

// Saul Tigh's "Declare Martial Law", once per game: the Admiral becomes President.
public record DeclareMartialLawEvent(int playerNumber) implements CharacterActionEvent {
}
