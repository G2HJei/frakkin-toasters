package xyz.zlatanov.frakkintoasters.event.deck;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

public class CreateDestinyDeckEventProcessor extends EventProcessor<CreateDestinyDeckEvent> {
    @Override
    public Followup process() {
        val decks = game.decks();
        decks.destiny()
                .addOnTop(decks.politics().draw(2))
                .addOnTop(decks.leadership().draw(2))
                .addOnTop(decks.tactics().draw(2))
                .addOnTop(decks.piloting().draw(2))
                .addOnTop(decks.engineering().draw(2))
                .addOnTop(decks.treachery().draw(2));
        return Followup.NONE;
    }
}
