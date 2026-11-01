package xyz.zlatanov.frakkintoasters.event.location.galactica;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;

// FTL Control (Galactica location)
// Action: Jump the fleet if the Jump Preparation track is not in the red zone.*
// *Might lose population.
public record FTLControlEvent(int playerNumber) implements PlayerEvent {
}
