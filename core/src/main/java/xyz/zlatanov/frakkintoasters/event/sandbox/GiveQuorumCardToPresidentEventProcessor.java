package xyz.zlatanov.frakkintoasters.event.sandbox;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.deck.DeckId;

public class GiveQuorumCardToPresidentEventProcessor extends EventProcessor<GiveQuorumCardToPresidentEvent> {

    @Override
    protected boolean isValid() {
        return game.president() != null && event.card() != null
                && game.decks().deck(DeckId.QUORUM).cards().contains(event.card());
    }

    @Override
    public Followup process() {
        ops.sandbox().giveQuorumCardToPresident(event.card());
        return Followup.NONE;
    }
}
