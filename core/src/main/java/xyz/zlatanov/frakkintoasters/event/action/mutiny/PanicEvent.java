package xyz.zlatanov.frakkintoasters.event.action.mutiny;

import xyz.zlatanov.frakkintoasters.event.action.MutinyCardEvent;

// Panic (Mutiny Card)
// Action: a civilian ship behind Galactica, then the player activates an unmanned viper, if he can. Discard it.
public record PanicEvent(int playerNumber) implements MutinyCardEvent {
}
