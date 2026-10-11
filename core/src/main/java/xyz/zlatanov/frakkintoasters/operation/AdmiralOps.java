package xyz.zlatanov.frakkintoasters.operation;

import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.character.Character;

public class AdmiralOps extends AbstractOps {

    AdmiralOps(Game game) {
        super(game);
    }

    public void assign(Character character) {
        game.admiral(character);
    }
}
