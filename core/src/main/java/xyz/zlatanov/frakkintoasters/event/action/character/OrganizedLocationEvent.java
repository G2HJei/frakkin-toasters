package xyz.zlatanov.frakkintoasters.event.action.character;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.event.Source;
import xyz.zlatanov.frakkintoasters.state.board.Location;

import java.util.List;

import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.ORGANIZED;

// The next location of "Organized", after the ones already used.
@Source(ability = ORGANIZED)
public record OrganizedLocationEvent(int playerNumber, Location location, List<Location> used) implements PlayerEvent {
}
