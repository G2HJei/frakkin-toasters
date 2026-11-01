package xyz.zlatanov.frakkintoasters.event.location.cylon;

import xyz.zlatanov.frakkintoasters.event.ActionEvent;

// Resurrection Ship (Cylon location, Daybreak)
// Draw only 1 Skill Card during your Draw Skill Cards step.
// Action: Draw 1 Super Crisis Card.
public record ResurrectionShipEvent(int playerNumber) implements ActionEvent {
}
