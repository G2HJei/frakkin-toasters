package xyz.zlatanov.frakkintoasters.event.sandbox;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.deck.DeckId;

public class GiveSuperCrisisCardToPlayerEventProcessor extends EventProcessor<GiveSuperCrisisCardToPlayerEvent> {

    @Override
    protected boolean isValid() {
        return isPlayer(event.targetPlayerNumber()) && event.card() != null
                && game.decks().deck(DeckId.SUPER_CRISIS).cards().contains(event.card());
    }

    @Override
    public Followup process() {
        ops.sandbox().giveSuperCrisisCard(event.targetPlayerNumber(), event.card());
        return Followup.NONE;
    }
}
