package xyz.zlatanov.frakkintoasters.event.action.title;

import xyz.zlatanov.frakkintoasters.event.ActionEvent;
import xyz.zlatanov.frakkintoasters.state.board.Location;

// The Admiral Title Card's action: launch a nuke at a space area, which spends it (rule 11.6.1).
public record LaunchNukeEvent(int playerNumber, Location area) implements ActionEvent {
}
