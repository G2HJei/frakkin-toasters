package xyz.zlatanov.frakkintoasters.event;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;

public class Discard1MutinyCardEventProcessor extends EventProcessor<Discard1MutinyCardEvent> {

    @Override
    public Followup process() {
        val cardToDiscard = event.cardToDiscard();
        player().mutinyCards()
                .remove(cardToDiscard)
                .discard(cardToDiscard);
        return Followup.NONE;
    }
}
