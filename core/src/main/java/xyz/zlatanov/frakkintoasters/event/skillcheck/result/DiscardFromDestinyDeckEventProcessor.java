package xyz.zlatanov.frakkintoasters.event.skillcheck.result;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

import java.util.ArrayList;

public class DiscardFromDestinyDeckEventProcessor extends EventProcessor<DiscardFromDestinyDeckEvent> {

    // Exactly 2 cards, or the whole deck if it has fewer. Equal cards are copies, so each takes one out of the deck.
    @Override
    protected boolean isValid() {
        val destiny = game.decks().destiny();
        val remaining = new ArrayList<>(destiny.cards());
        return event.cards() != null
                && event.cards().size() == Math.min(2, destiny.size())
                && event.cards().stream().allMatch(remaining::remove);
    }

    @Override
    public Followup process() {
        ops.skillCheck().discardFromDestinyDeck(event.cards());
        return Followup.NONE;
    }
}
