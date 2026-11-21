package xyz.zlatanov.frakkintoasters.event.action.character;

import xyz.zlatanov.frakkintoasters.event.action.CharacterActionEvent;

// Helena Cain's "Blind Jump", once per game, at 6 distance or less: 2 civilian ships are drawn and destroyed and
// the fleet jumps at once, wherever the jump preparation token is. The Admiral draws only 1 Destination Card.
public record BlindJumpEvent(int playerNumber) implements CharacterActionEvent {
}
