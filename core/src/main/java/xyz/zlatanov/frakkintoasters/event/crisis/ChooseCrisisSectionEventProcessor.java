package xyz.zlatanov.frakkintoasters.event.crisis;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

public class ChooseCrisisSectionEventProcessor extends EventProcessor<ChooseCrisisSectionEvent> {

    @Override
    public Followup process() {
        return new CrisisCardFollowups(game).followup(event.card(), event.section());
    }
}
