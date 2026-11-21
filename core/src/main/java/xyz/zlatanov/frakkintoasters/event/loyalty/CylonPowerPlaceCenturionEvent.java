package xyz.zlatanov.frakkintoasters.event.loyalty;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;

// "Can place a centurion": a centurion at the start of the Boarding Party track.
public record CylonPowerPlaceCenturionEvent(int playerNumber) implements PlayerEvent {
}
