package xyz.zlatanov.frakkintoasters.event.location.galactica;

import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.FollowupUtils;
import xyz.zlatanov.frakkintoasters.event.location.LocationActionProcessor;

import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType.BRIG;

public class BrigEventProcessor extends LocationActionProcessor<BrigEvent> {

    @Override
    public Followup process() {
        // The pass result moves the character who took the action, so he is the target.
        ops.skillCheck().start(BRIG, player.number());
        return FollowupUtils.beforeSkillCheckFollowups(game);
    }
}
