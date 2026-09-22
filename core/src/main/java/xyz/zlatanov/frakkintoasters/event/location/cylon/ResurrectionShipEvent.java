package xyz.zlatanov.frakkintoasters.event.location.cylon;

import xyz.zlatanov.frakkintoasters.event.ActionEvent;

public record ResurrectionShipEvent(int playerNumber) implements ActionEvent {
}
