package xyz.zlatanov.frakkintoasters.event.character;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.event.Source;

import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.FRIENDS_IN_LOW_PLACES;

// Tom Zarek's "Friends in Low Places": whoever activates the "Brig", he may make its check 2 harder or 2 easier. The
// "Administration" half has nothing to change: that location has no check here.
//
// @param adjustment 2 or -2
@Source(ability = FRIENDS_IN_LOW_PLACES)
public record FriendsInLowPlacesEvent(int playerNumber, int adjustment) implements PlayerEvent {
}
