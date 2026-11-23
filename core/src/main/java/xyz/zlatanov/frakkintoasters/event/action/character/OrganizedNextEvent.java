package xyz.zlatanov.frakkintoasters.event.action.character;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.event.Source;
import xyz.zlatanov.frakkintoasters.state.board.Location;

import java.util.List;

import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.ORGANIZED;

// After one location of "Organized": the next one, from the locations he can act at then.
@Source(ability = ORGANIZED)
public record OrganizedNextEvent(int playerNumber, List<Location> used) implements PlayerEvent {
}
