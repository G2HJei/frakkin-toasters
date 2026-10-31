package xyz.zlatanov.frakkintoasters.event.skillcheck;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

public class DrawAndResolveRecklessCardsEventProcessor extends EventProcessor<DrawAndResolveRecklessCardsEvent> {

    @Override
    public Followup process() {
        return Followup.NONE;
    }
}
