package xyz.zlatanov.frakkintoasters.event.location.galactica;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

import static xyz.zlatanov.frakkintoasters.event.Followup.skillCheckFollowup;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheck.ADMIRALS_QUARTERS;

public class AdmiralsQuartersEventProcessor extends EventProcessor<AdmiralsQuartersEvent> {

    @Override
    public Followup process() {
        game.activeSkillCheck(ADMIRALS_QUARTERS);
        return skillCheckFollowup(game);
    }
}
