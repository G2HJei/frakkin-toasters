package xyz.zlatanov.frakkintoasters.event.action.character;

import xyz.zlatanov.frakkintoasters.event.action.CharacterActionEvent;

// D'Anna Biers' "Don't Trust Anyone", once per game: she draws 2 Super Crisis Cards.
public record DontTrustAnyoneEvent(int playerNumber) implements CharacterActionEvent {
}
