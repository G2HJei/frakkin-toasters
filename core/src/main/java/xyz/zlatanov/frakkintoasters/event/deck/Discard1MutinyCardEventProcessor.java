package xyz.zlatanov.frakkintoasters.event.deck;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

public class Discard1MutinyCardEventProcessor extends EventProcessor<Discard1MutinyCardEvent> {

    @Override
    public Followup process() {
        val cardToDiscard = event.cardToDiscard();
        player.mutinyCards()
                .remove(cardToDiscard)
                .discard(cardToDiscard);
        return Followup.NONE;
    }
}
