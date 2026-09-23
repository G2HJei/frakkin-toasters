package xyz.zlatanov.frakkintoasters.event.skillcheck;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

public class PlaySkillsEventProcessor extends EventProcessor<PlaySkillsEvent> {

    @Override
    public Followup process() {
        game.activeSkillCheck().cards().addOnTop(event.skillCards());
        return Followup.NONE;
    }
}
