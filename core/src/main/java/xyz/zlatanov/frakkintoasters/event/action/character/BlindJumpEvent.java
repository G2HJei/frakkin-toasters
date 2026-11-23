package xyz.zlatanov.frakkintoasters.event.action.character;

import xyz.zlatanov.frakkintoasters.event.Source;
import xyz.zlatanov.frakkintoasters.event.action.CharacterActionEvent;

import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.BLIND_JUMP;

// Helena Cain's "Blind Jump", once per game, at 6 distance or less: 2 civilian ships are drawn and destroyed and
// the fleet jumps at once, wherever the jump preparation token is. The Admiral draws only 1 Destination Card.
@Source(ability = BLIND_JUMP)
public record BlindJumpEvent(int playerNumber) implements CharacterActionEvent {
}
