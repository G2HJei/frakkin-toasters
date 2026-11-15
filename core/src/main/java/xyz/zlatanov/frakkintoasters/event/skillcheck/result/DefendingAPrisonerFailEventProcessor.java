package xyz.zlatanov.frakkintoasters.event.skillcheck.result;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.rollDie;

public class DefendingAPrisonerFailEventProcessor extends EventProcessor<DefendingAPrisonerFailEvent> {

    @Override
    public Followup process() {
        return rollDie(new DefendingAPrisonerFailResultEvent());
    }
}
