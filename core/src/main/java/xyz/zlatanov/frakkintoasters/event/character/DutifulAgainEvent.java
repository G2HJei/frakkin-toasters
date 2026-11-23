package xyz.zlatanov.frakkintoasters.event.character;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.event.Source;
import xyz.zlatanov.frakkintoasters.state.board.Location;

import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.DUTIFUL;

// After the card "Dutiful" costs Louis Hoshi, the location's action again, if he still can take it: a "Bait" he
// discarded draws a Mutiny Card, which may have sent him to the "Brig" in between.
@Source(ability = DUTIFUL)
public record DutifulAgainEvent(int playerNumber, Location location) implements PlayerEvent {
}
