package xyz.zlatanov.frakkintoasters.event;

// A pilot activates his own viper, e.g. as his action. A player who isn't piloting activates an unmanned one, e.g. at
// "Command".
public record ActivateViperEvent(int playerNumber, ViperActivation activation) implements PlayerEvent {
}
