package xyz.zlatanov.frakkintoasters.event.crisis;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

public class KeepCrisisCardInPlayEventProcessor extends EventProcessor<KeepCrisisCardInPlayEvent> {

    @Override
    protected boolean isValid() {
        return event.skillCheckType() != null;
    }

    // The lasting effects are read where they apply: e.g. LookAtLoyaltyCardEventProcessor for "Detector Sabotage".
    @Override
    public Followup process() {
        ops.keepCrisisCardInPlay(event.skillCheckType());
        return Followup.NONE;
    }
}
