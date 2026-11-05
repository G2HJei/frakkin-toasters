package xyz.zlatanov.frakkintoasters.event.deck;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor;

import java.util.Collections;

import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.TREACHERY;

public class ShuffleTreacheryIntoDestinyDeckEventProcessor extends EventProcessor<ShuffleTreacheryIntoDestinyDeckEvent> {

    @Override
    public Followup process() {
        ops.skillCheck().shuffleIntoDestinyDeck(Collections.nCopies(event.count(), TREACHERY)
                .toArray(SkillCardColor[]::new));
        return Followup.NONE;
    }
}
