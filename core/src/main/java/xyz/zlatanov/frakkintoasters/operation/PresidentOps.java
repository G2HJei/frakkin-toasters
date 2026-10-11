package xyz.zlatanov.frakkintoasters.operation;

import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.character.Character;

public class PresidentOps extends AbstractOps {

    PresidentOps(Game game) {
        super(game);
    }

    public void assign(Character character) {
        game.president(character);
    }

    public void drawQuorumCard() {
        game.presidentHand().addOnTop(game.decks().quorum().draw());
    }
}
