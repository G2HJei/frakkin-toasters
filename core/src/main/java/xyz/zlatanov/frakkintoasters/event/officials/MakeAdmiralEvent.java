package xyz.zlatanov.frakkintoasters.event.officials;

import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.character.Character;

public record MakeAdmiralEvent(Character character) implements Event {

    @Override
    public void apply(Game game) {
        game.admiral(character);
    }
}
