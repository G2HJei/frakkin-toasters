package xyz.zlatanov.frakkintoasters.event.sandbox;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.deck.DeckId;

public class PutDestinationCardOnTopEventProcessor extends EventProcessor<PutDestinationCardOnTopEvent> {

    @Override
    protected boolean isValid() {
        return event.card() != null && game.decks().deck(DeckId.DESTINATION).cards().contains(event.card());
    }

    @Override
    public Followup process() {
        ops.sandbox().stackOnTop(DeckId.DESTINATION, event.card());
        return Followup.NONE;
    }
}
