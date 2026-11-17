package xyz.zlatanov.frakkintoasters.event.sandbox;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.deck.DeckId;

public class PutDestinyCardOnTopEventProcessor extends EventProcessor<PutDestinyCardOnTopEvent> {

    @Override
    protected boolean isValid() {
        return event.card() != null && event.card().type() != null
                && (game.decks().destiny().cards().contains(event.card())
                || game.decks().deck(DeckId.of(event.card().type().color())).cards().contains(event.card()));
    }

    @Override
    public Followup process() {
        ops.sandbox().stackDestiny(event.card());
        return Followup.NONE;
    }
}
