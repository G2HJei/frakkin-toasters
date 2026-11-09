package xyz.zlatanov.frakkintoasters.event.location.galactica;

import xyz.zlatanov.frakkintoasters.event.ActionEvent;
import xyz.zlatanov.frakkintoasters.state.board.Location;

import static xyz.zlatanov.frakkintoasters.state.board.Location.ADMIRALS_QUARTERS;

// Admiral's Quarters (Galactica location) - skill check difficulty: 7, colors: LEADERSHIP, TACTICS
// Action: Choose a character, then pass this skill check to send him to the "Brig."
public record AdmiralsQuartersEvent(int playerNumber, int targetPlayer) implements ActionEvent {

    @Override
    public Location location() {
        return ADMIRALS_QUARTERS;
    }
}
