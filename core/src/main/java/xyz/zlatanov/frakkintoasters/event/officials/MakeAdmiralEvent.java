package xyz.zlatanov.frakkintoasters.event.officials;

import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.state.character.Character;

public record MakeAdmiralEvent(Character character) implements Event {
}
