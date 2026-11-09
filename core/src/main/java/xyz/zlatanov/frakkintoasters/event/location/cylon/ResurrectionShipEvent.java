package xyz.zlatanov.frakkintoasters.event.location.cylon;

import xyz.zlatanov.frakkintoasters.event.ActionEvent;
import xyz.zlatanov.frakkintoasters.state.board.Location;

import static xyz.zlatanov.frakkintoasters.state.board.Location.RESURRECTION_SHIP;

// Resurrection Ship (Cylon location, Daybreak)
// Draw only 1 Skill Card during your Draw Skill Cards step.
// Action: Draw 1 Super Crisis Card.
public record ResurrectionShipEvent(int playerNumber) implements ActionEvent {

    @Override
    public Location location() {
        return RESURRECTION_SHIP;
    }
}
