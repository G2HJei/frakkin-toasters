package xyz.zlatanov.frakkintoasters.event.action.character;

import xyz.zlatanov.frakkintoasters.event.Source;
import xyz.zlatanov.frakkintoasters.event.action.CharacterActionEvent;

import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.CRYPTIC_MESSAGE;

// Leoben Conoy's "Cryptic Message", once per game: he and another player swap their Skill Card hands.
@Source(ability = CRYPTIC_MESSAGE)
public record CrypticMessageEvent(int playerNumber, int targetPlayerNumber) implements CharacterActionEvent {
}
