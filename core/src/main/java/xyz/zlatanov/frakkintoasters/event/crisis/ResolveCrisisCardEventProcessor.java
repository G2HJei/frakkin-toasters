package xyz.zlatanov.frakkintoasters.event.crisis;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

public class ResolveCrisisCardEventProcessor extends EventProcessor<ResolveCrisisCardEvent> {

    @Override
    public Followup process() {
        return new CrisisCardFollowups(game).choice(event.card());
    }
}
