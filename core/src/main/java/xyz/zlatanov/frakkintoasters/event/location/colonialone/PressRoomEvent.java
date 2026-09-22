package xyz.zlatanov.frakkintoasters.event.location.colonialone;

import xyz.zlatanov.frakkintoasters.event.ActionEvent;

public record PressRoomEvent(int playerNumber, int targetPlayer) implements ActionEvent {

}
