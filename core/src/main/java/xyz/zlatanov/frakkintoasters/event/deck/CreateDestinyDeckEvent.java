package xyz.zlatanov.frakkintoasters.event.deck;

import lombok.val;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.Game;

public record CreateDestinyDeckEvent() implements Event {
    @Override
    public Followup apply(Game game) {
        val decks = game.decks();
        decks.destiny().add(decks.politics().draw(2));
        decks.destiny().add(decks.leadership().draw(2));
        decks.destiny().add(decks.tactics().draw(2));
        decks.destiny().add(decks.piloting().draw(2));
        decks.destiny().add(decks.engineering().draw(2));
        decks.destiny().add(decks.treachery().draw(2));
        return Followup.NONE;
    }
}
