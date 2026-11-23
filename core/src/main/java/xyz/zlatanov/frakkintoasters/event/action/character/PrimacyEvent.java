package xyz.zlatanov.frakkintoasters.event.action.character;

import xyz.zlatanov.frakkintoasters.event.Source;
import xyz.zlatanov.frakkintoasters.event.action.CharacterActionEvent;

import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.PRIMACY;

// Cavil's "Primacy": a basestar in front of Galactica, or, with every basestar in play, he may take one off the
// main board instead (removedBasestarId). He may then also put 3 raiders in front and a civilian ship behind.
@Source(ability = PRIMACY)
public record PrimacyEvent(int playerNumber, Integer removedBasestarId, boolean placeRaidersAndCivilianShip) implements CharacterActionEvent {
}
