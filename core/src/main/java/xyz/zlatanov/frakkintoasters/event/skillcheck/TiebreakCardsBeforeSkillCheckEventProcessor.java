package xyz.zlatanov.frakkintoasters.event.skillcheck;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

public class TiebreakCardsBeforeSkillCheckEventProcessor extends EventProcessor<TiebreakCardsBeforeSkillCheckEvent> {


    @Override
    public Followup process() {
        return Followup.NONE;
    }
}
