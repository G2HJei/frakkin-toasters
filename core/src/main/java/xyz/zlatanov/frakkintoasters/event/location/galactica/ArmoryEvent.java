package xyz.zlatanov.frakkintoasters.event.location.galactica;

import xyz.zlatanov.frakkintoasters.event.ActionEvent;
import xyz.zlatanov.frakkintoasters.state.board.Location;

import static xyz.zlatanov.frakkintoasters.state.board.Location.ARMORY;

// Armory (Galactica location)
// Action: Attack a centurion on the Boarding Party track (destroyed on roll of 7-8).
public record ArmoryEvent(int playerNumber, int centurionId) implements ActionEvent {

    @Override
    public Location location() {
        return ARMORY;
    }
}
