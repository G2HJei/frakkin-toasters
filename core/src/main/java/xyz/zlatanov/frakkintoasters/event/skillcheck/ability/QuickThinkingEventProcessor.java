package xyz.zlatanov.frakkintoasters.event.skillcheck.ability;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

public class QuickThinkingEventProcessor extends EventProcessor<QuickThinkingEvent> {
    @Override
    protected boolean isValid() {
        val card = event.removedCard();
        return card == null ||
                card.value() <= 3;
    }

    @Override
    public Followup process() {
        if (event.removedCard() != null) {
            ops.skillCheck().giveCardToPlayer(event.removedCard(), player.number());
        }
        return Followup.NONE;
    }
}
