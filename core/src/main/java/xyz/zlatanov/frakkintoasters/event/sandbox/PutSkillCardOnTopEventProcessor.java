package xyz.zlatanov.frakkintoasters.event.sandbox;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.deck.DeckId;

public class PutSkillCardOnTopEventProcessor extends EventProcessor<PutSkillCardOnTopEvent> {

    @Override
    protected boolean isValid() {
        return event.card() != null && event.card().type() != null
                && game.decks().deck(DeckId.of(event.card().type().color())).cards().contains(event.card());
    }

    @Override
    public Followup process() {
        ops.sandbox().stackOnTop(DeckId.of(event.card().type().color()), event.card());
        return Followup.NONE;
    }
}
