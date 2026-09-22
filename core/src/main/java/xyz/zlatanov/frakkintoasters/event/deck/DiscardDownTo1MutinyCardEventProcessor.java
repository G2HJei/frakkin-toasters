package xyz.zlatanov.frakkintoasters.event.deck;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

public class DiscardDownTo1MutinyCardEventProcessor extends EventProcessor<DiscardDownTo1MutinyCardEvent> {

    @Override
    public Followup process() {
        val mutinyCardDeck = player.mutinyCards();
        val cardsToDiscard = mutinyCardDeck
                .cards()
                .stream()
                .filter(c -> c != event.cardToKeep())
                .toList();
        mutinyCardDeck
                .remove(cardsToDiscard)
                .discard(cardsToDiscard);
        return Followup.NONE;
    }
}
