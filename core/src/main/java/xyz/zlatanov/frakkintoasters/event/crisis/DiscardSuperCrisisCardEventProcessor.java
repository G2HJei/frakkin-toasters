package xyz.zlatanov.frakkintoasters.event.crisis;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import lombok.val;
import xyz.zlatanov.frakkintoasters.event.Followup;

public class DiscardSuperCrisisCardEventProcessor extends EventProcessor<DiscardSuperCrisisCardEvent> {

    @Override
    protected boolean isValid() {
        return event.card() != null;
    }

    @Override
    public Followup process() {
        // "Keep this card in play": "Bomb on Colonial One", and "The Farm" on a fail.
        val skillCheck = event.card().skillCheck();
        if (skillCheck == null || !game.crisisCardsInPlay().contains(skillCheck)) {
            ops.discardSuperCrisisCard(event.card());
        }
        return Followup.NONE;
    }
}
