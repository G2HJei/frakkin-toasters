package xyz.zlatanov.frakkintoasters.event.location.galactica;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.FollowupUtils;

import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType.ADMIRALS_QUARTERS;

public class AdmiralsQuartersEventProcessor extends EventProcessor<AdmiralsQuartersEvent> {

    @Override
    public Followup process() {
        ops.skillCheck().start(ADMIRALS_QUARTERS);
        return FollowupUtils.skillCheckFollowup(game);
    }
}
