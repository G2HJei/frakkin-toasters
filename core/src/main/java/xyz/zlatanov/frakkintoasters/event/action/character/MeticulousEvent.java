package xyz.zlatanov.frakkintoasters.event.action.character;

import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.event.Source;
import xyz.zlatanov.frakkintoasters.state.board.Location;

import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.METICULOUS;

// Where Aaron Doral goes when an action ends his Infiltration: the "Resurrection Ship" as usual, or with
// "Meticulous", once per game, another Cylon location, and then he takes another action.
@Source(ability = METICULOUS)
public record MeticulousEvent(int playerNumber, Location location) implements PlayerEvent {
}
