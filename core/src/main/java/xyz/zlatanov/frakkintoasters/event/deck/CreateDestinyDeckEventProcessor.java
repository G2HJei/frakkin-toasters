package xyz.zlatanov.frakkintoasters.event.deck;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.Game;

public class CreateDestinyDeckEventProcessor extends EventProcessor<CreateDestinyDeckEvent> {
    @Override
    public Followup process() {
        createNewDestinyDeck(game);
        return Followup.NONE;
    }
    
    public static void createNewDestinyDeck(Game game) {
        val decks = game.decks();
        game.decks().destiny()
                .addOnTop(decks.politics().draw(2))
                .addOnTop(decks.leadership().draw(2))
                .addOnTop(decks.tactics().draw(2))
                .addOnTop(decks.piloting().draw(2))
                .addOnTop(decks.engineering().draw(2))
                .addOnTop(decks.treachery().draw(2));
        game.decks().destiny().shuffle();
    }
}
