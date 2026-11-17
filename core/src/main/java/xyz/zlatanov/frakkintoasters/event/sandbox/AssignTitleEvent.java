package xyz.zlatanov.frakkintoasters.event.sandbox;

import xyz.zlatanov.frakkintoasters.state.character.Character;

// Gives a title to a character of the game, without the line of succession.
public record AssignTitleEvent(Title title, Character character) implements SandboxEvent {

    public enum Title {
        PRESIDENT,
        ADMIRAL,
        CAG
    }
}
