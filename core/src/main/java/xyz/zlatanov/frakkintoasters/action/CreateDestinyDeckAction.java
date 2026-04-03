package xyz.zlatanov.frakkintoasters.action;

import lombok.val;
import xyz.zlatanov.frakkintoasters.Game;

public record CreateDestinyDeckAction() implements Action {
    @Override
    public void apply(Game game) {
        val decks = game.decks();
        decks.destiny().add(decks.politics().draw(2));
        decks.destiny().add(decks.leadership().draw(2));
        decks.destiny().add(decks.tactics().draw(2));
        decks.destiny().add(decks.piloting().draw(2));
        decks.destiny().add(decks.engineering().draw(2));
        decks.destiny().add(decks.treachery().draw(2));
    }
}
