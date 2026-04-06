package xyz.zlatanov.frakkintoasters.event.deck;

import lombok.val;
import xyz.zlatanov.frakkintoasters.Game;
import xyz.zlatanov.frakkintoasters.event.Event;

public record CreateDestinyDeckEvent() implements Event {
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
