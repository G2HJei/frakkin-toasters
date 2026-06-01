package xyz.zlatanov.frakkintoasters.event.action;

import xyz.zlatanov.frakkintoasters.event.ActionEvent;

public record PressRoomEvent(int playerNumber, int targetPlayer) implements ActionEvent {

}
