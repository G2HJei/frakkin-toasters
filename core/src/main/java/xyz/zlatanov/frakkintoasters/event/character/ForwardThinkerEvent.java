package xyz.zlatanov.frakkintoasters.event.character;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.event.Source;

import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.FORWARD_THINKER;

// Lee Adama's "Forward Thinker": after his "Executive Order" and the moves and actions of the player he chose, he may
// activate his current location.
@Source(ability = FORWARD_THINKER)
public record ForwardThinkerEvent(int playerNumber) implements PlayerEvent {
}
