package xyz.zlatanov.frakkintoasters.event.crisis;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

public class ResolveCrisisSectionThenEventProcessor extends EventProcessor<ResolveCrisisSectionThenEvent> {

    @Override
    protected boolean isValid() {
        return event.card() != null && event.section() != null
                && CrisisCardFollowups.hasSecondPart(event.card(), event.section());
    }

    @Override
    public Followup process() {
        return new CrisisCardFollowups(game).secondPart(event.card(), event.section());
    }
}
