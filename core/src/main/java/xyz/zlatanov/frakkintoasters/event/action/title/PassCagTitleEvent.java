package xyz.zlatanov.frakkintoasters.event.action.title;

import xyz.zlatanov.frakkintoasters.event.ActionEvent;
import xyz.zlatanov.frakkintoasters.event.ViperActivation;

// The CAG Title Card's first action: activate an unmanned viper, then hand the CAG title to another human player.
public record PassCagTitleEvent(int playerNumber, ViperActivation activation, int targetPlayerNumber) implements ActionEvent {
}
