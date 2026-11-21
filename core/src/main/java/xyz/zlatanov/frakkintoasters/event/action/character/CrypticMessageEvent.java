package xyz.zlatanov.frakkintoasters.event.action.character;

import xyz.zlatanov.frakkintoasters.event.action.CharacterActionEvent;

// Leoben Conoy's "Cryptic Message", once per game: he and another player swap their Skill Card hands.
public record CrypticMessageEvent(int playerNumber, int targetPlayerNumber) implements CharacterActionEvent {
}
