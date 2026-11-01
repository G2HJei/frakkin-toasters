package xyz.zlatanov.frakkintoasters.event.location.galactica;

import xyz.zlatanov.frakkintoasters.event.ActionEvent;

// Hangar Deck (Galactica location)
// Action: Launch yourself in a viper. You may then take 1 more action.
public record HangarDeckEvent(int playerNumber) implements ActionEvent {

}
