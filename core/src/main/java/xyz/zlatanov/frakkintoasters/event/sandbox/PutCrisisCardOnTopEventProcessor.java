package xyz.zlatanov.frakkintoasters.event.sandbox;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.deck.DeckId;

public class PutCrisisCardOnTopEventProcessor extends EventProcessor<PutCrisisCardOnTopEvent> {

    @Override
    protected boolean isValid() {
        return event.card() != null && game.decks().deck(DeckId.CRISIS).cards().contains(event.card());
    }

    @Override
    public Followup process() {
        ops.sandbox().stackOnTop(DeckId.CRISIS, event.card());
        return Followup.NONE;
    }
}
