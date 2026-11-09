package xyz.zlatanov.frakkintoasters.event.location.galactica;

import xyz.zlatanov.frakkintoasters.event.ActionEvent;
import xyz.zlatanov.frakkintoasters.state.board.Location;

import static xyz.zlatanov.frakkintoasters.state.board.Location.WEAPONS_CONTROL;

// Weapons Control (Galactica location)
// Action: Attack 1 Cylon ship with Galactica.
public record WeaponsControlEvent(int playerNumber, int cylonShipId) implements ActionEvent {

    @Override
    public Location location() {
        return WEAPONS_CONTROL;
    }
}
