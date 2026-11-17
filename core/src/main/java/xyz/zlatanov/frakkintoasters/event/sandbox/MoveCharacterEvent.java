package xyz.zlatanov.frakkintoasters.event.sandbox;

import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.character.Character;

// Moves a character of the game to a location that isn't in space, without the rules of movement.
public record MoveCharacterEvent(Character character, Location destination) implements SandboxEvent {
}
