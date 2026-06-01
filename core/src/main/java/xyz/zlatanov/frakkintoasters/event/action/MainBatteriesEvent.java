package xyz.zlatanov.frakkintoasters.event.action;

import xyz.zlatanov.frakkintoasters.event.ActionEvent;
import xyz.zlatanov.frakkintoasters.state.board.Location;

public record MainBatteriesEvent(int playerNumber, Location spaceLocation) implements ActionEvent {
}
