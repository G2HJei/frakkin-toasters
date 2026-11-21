package xyz.zlatanov.frakkintoasters.event.action.mutiny;

import xyz.zlatanov.frakkintoasters.event.action.MutinyCardEvent;

// Scavenging for Parts (Mutiny Card)
// Action: damage Galactica, then, if there is one, a civilian ship in space of the player's choice is shuffled back into
// the civilian ship deck. Discard it.
//
// @param civilianShipId the civilian ship, null when there is none in space
public record ScavengingForPartsEvent(int playerNumber, Integer civilianShipId) implements MutinyCardEvent {
}
