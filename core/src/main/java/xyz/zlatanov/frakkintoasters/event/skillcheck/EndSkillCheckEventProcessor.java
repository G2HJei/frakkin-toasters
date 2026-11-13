package xyz.zlatanov.frakkintoasters.event.skillcheck;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

public class EndSkillCheckEventProcessor extends EventProcessor<EndSkillCheckEvent> {

    @Override
    protected boolean isValid() {
        return game.activeSkillCheck() != null && game.activeSkillCheck().result() != null;
    }

    @Override
    public Followup process() {
        ops.skillCheck().end();
        return Followup.NONE;
    }
}
