package xyz.zlatanov.frakkintoasters.event.location.colonialone;

import xyz.zlatanov.frakkintoasters.event.ActionEvent;

public record QuorumChamberEvent(int playerNumber) implements ActionEvent {
}
