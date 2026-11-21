package xyz.zlatanov.frakkintoasters.event;

// The player activates an unmanned viper (rule 11.1.1) because a card says so, e.g. "Panic". Unlike ActivateViperEvent,
// a pilot activates an unmanned viper too, not his own.
public record ActivateUnmannedViperEvent(int playerNumber, ViperActivation activation) implements PlayerEvent {
}
