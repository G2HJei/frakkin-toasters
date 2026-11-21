package xyz.zlatanov.frakkintoasters.event.action.character;

import xyz.zlatanov.frakkintoasters.event.action.CharacterActionEvent;

// Cavil's "Primacy": a basestar in front of Galactica, or, with every basestar in play, he may take one off the
// main board instead (removedBasestarId). He may then also put 3 raiders in front and a civilian ship behind.
public record PrimacyEvent(int playerNumber, Integer removedBasestarId, boolean placeRaidersAndCivilianShip) implements CharacterActionEvent {
}
