package xyz.zlatanov.frakkintoasters.event.skillcheck.ability;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

public class QuickThinkingEventProcessor extends EventProcessor<QuickThinkingEvent> {
    @Override
    protected boolean isValid() {
        return event.cardToRemove() == null || game.activeSkillCheck()
                .cardValue(event.cardToRemove()) <= 3;
    }

    @Override
    public Followup process() {
        if (event.cardToRemove() != null) {
            ops.skillCheck().giveCardToPlayer(event.cardToRemove(), game.currentPlayerNumber());
        }
        return Followup.NONE;
    }
}
