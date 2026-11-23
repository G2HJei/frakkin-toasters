package xyz.zlatanov.frakkintoasters.event.action.character;

import xyz.zlatanov.frakkintoasters.event.Source;
import xyz.zlatanov.frakkintoasters.event.action.CharacterActionEvent;

import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.DONT_TRUST_ANYONE;

// D'Anna Biers' "Don't Trust Anyone", once per game: she draws 2 Super Crisis Cards.
@Source(ability = DONT_TRUST_ANYONE)
public record DontTrustAnyoneEvent(int playerNumber) implements CharacterActionEvent {
}
