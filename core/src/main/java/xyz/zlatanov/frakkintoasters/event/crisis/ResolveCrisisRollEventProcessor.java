package xyz.zlatanov.frakkintoasters.event.crisis;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

public class ResolveCrisisRollEventProcessor extends EventProcessor<ResolveCrisisRollEvent> {

    @Override
    public Followup process() {
        return new CrisisCardFollowups(game).rollResult(event.card(), event.section(), game.turn().dieRoll().result());
    }
}
