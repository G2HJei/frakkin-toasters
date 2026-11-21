package xyz.zlatanov.frakkintoasters.event.action.title;

import xyz.zlatanov.frakkintoasters.event.ActionEvent;
import xyz.zlatanov.frakkintoasters.event.ViperActivation;

// The CAG Title Card's second action: once a turn, while piloting a viper, activate an unmanned viper, then take another
// action.
public record CagPilotActionEvent(int playerNumber, ViperActivation activation) implements ActionEvent {
}
