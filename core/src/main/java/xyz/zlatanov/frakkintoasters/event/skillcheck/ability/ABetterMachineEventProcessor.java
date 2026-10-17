package xyz.zlatanov.frakkintoasters.event.skillcheck.ability;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.TREACHERY;

public class ABetterMachineEventProcessor extends EventProcessor<ABetterMachineEvent> {

    @Override
    public Followup process() {
        ops.skillCheck().shuffleIntoDestinyDeck(TREACHERY, TREACHERY);
        return Followup.NONE;
    }
}
