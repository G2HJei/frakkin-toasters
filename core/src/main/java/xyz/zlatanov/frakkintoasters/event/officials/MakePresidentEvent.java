package xyz.zlatanov.frakkintoasters.event.officials;

import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.character.Character;

import java.util.List;

public record MakePresidentEvent(Character character) implements Event {

    @Override
    public List<Followup> apply(Game game) {
        game.president(character);
        return List.of();
    }
}
