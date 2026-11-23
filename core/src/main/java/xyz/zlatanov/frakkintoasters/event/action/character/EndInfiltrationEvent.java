package xyz.zlatanov.frakkintoasters.event.action.character;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.event.Source;

import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.VISIONS;

// An ability ends the player's Infiltration at the "Resurrection Ship", e.g. "Visions", unless something ended
// it first.
@Source(ability = VISIONS)
public record EndInfiltrationEvent(int playerNumber) implements PlayerEvent {
}
