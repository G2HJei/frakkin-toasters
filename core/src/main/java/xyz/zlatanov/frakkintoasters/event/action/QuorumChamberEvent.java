package xyz.zlatanov.frakkintoasters.event.action;

import xyz.zlatanov.frakkintoasters.event.ActionEvent;

public record QuorumChamberEvent(int playerNumber) implements ActionEvent {
}
