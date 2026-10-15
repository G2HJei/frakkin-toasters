package xyz.zlatanov.frakkintoasters.operation;

import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.character.Character;

public class TitleOps extends GameOps {
    public TitleOps(Game game) {
        super(game);
    }

    public void assignAdmiral(Character character) {
        game.admiral(character);
    }

    public void assignCag(Character character) {
        game.cag(character);
    }

    public void assignPresident(Character character) {
        game.president(character);
    }

    public void drawQuorumCard() {
        game.presidentHand().addOnTop(game.decks().quorum().draw());
    }

}
