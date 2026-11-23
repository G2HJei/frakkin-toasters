package xyz.zlatanov.frakkintoasters.event.character;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.event.Source;

import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.CYLON_HATRED;

// Saul Tigh's "Cylon Hatred": whoever activates the "Admiral's Quarters", he may make its check 3 easier.
@Source(ability = CYLON_HATRED)
public record CylonHatredEvent(int playerNumber) implements PlayerEvent {
}
