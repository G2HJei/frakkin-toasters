package xyz.zlatanov.frakkintoasters.event.action;

import xyz.zlatanov.frakkintoasters.event.ActionEvent;

public record ResurrectionShipEvent(int playerNumber) implements ActionEvent {
}
