package xyz.zlatanov.frakkintoasters.event;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;

public class DiscardDownTo1MutinyCardEventProcessor extends EventProcessor<DiscardDownTo1MutinyCardEvent> {

    @Override
    public Followup process() {
        val mutinyCardDeck = player().mutinyCards();
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
