package xyz.zlatanov.frakkintoasters.engine;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

public class GiveSkillCardEventProcessor extends EventProcessor<GiveSkillCardEvent> {

    @Override
    public Followup process() {
        ops.player(event.playerNumber()).receiveSkillCard(event.card());
        return Followup.NONE;
    }
}
