package xyz.zlatanov.frakkintoasters.event.character;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;

// Tom Zarek's "Friends in Low Places": whoever activates the "Brig", he may make its check 2 harder or 2 easier. The
// "Administration" half has nothing to change: that location has no check here.
//
// @param adjustment 2 or -2
public record FriendsInLowPlacesEvent(int playerNumber, int adjustment) implements PlayerEvent {
}
