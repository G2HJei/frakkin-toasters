package xyz.zlatanov.frakkintoasters.event.location.colonialone;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.POLITICS;

public class PresidentsOfficeEventProcessor extends EventProcessor<PresidentsOfficeEvent> {
    @Override
    public Followup process() {
        ops.player(event.playerNumber()).drawSkillCards(POLITICS, POLITICS);
        return Followup.NONE;
    }

}
