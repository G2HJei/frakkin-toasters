package xyz.zlatanov.frakkintoasters.event.skillcheck;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;

public class RevealCardsBeforeSkillCheckEventProcessor extends EventProcessor<RevealCardsBeforeSkillCheckEvent> {

    @Override
    public Followup process() {
        return all(new PlayFromDestinyDeckEvent());
    }
}
