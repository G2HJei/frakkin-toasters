package xyz.zlatanov.frakkintoasters.event.skillcheck;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;

public class ResolveSkillCheckResultEventProcessor extends EventProcessor<ResolveSkillCheckResultEvent> {

    @Override
    protected boolean isValid() {
        return game.activeSkillCheck().result() != null;
    }

    @Override
    public Followup process() {
        val skillCheck = game.activeSkillCheck();
        if (skillCheck.passResultReplaced() || skillCheck.failResultPrevented()) {
            return NONE;
        }
        return new SkillCheckResultFollowups(game).followup();
    }
}
