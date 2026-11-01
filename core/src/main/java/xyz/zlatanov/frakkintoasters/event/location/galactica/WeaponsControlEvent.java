package xyz.zlatanov.frakkintoasters.event.location.galactica;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;

// Weapons Control (Galactica location)
// Action: Attack 1 Cylon ship with Galactica.
public record WeaponsControlEvent(int playerNumber, int cylonShipId) implements PlayerEvent {
}
