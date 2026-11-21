package xyz.zlatanov.frakkintoasters.event.character;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.state.crisis.CylonActivation;

// Simon O'Neill's "Modifications" (once per game): at the start of an Activate Cylon Ships step, he picks the Cylon ship
// activation instead of the Crisis Card's icon.
public record ModificationsEvent(int playerNumber, CylonActivation activation) implements PlayerEvent {
}
