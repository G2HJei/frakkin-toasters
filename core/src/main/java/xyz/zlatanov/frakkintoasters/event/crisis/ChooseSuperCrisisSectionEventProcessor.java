package xyz.zlatanov.frakkintoasters.event.crisis;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

public class ChooseSuperCrisisSectionEventProcessor extends EventProcessor<ChooseSuperCrisisSectionEvent> {

    @Override
    protected boolean isValid() {
        return event.card() != null && event.card().chooser() != null && event.section() != null;
    }

    @Override
    public Followup process() {
        return new SuperCrisisCardFollowups(game).followup(event.card(), event.section());
    }
}
