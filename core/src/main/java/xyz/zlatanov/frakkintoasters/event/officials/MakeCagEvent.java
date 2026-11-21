package xyz.zlatanov.frakkintoasters.event.officials;

import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.state.character.Character;

// The character gets the CAG title, e.g. through a "Presidential Order".
public record MakeCagEvent(Character character) implements Event {
}
