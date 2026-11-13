package xyz.zlatanov.frakkintoasters.event.player;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

public class DiscardAllSkillCardsEventProcessor extends EventProcessor<DiscardAllSkillCardsEvent> {

    // Discarding the whole hand is not a choice, so "Bait" and "Dradis Contact" don't make the player draw a Mutiny Card
    // (rule A2.3.8.1).
    @Override
    public Followup process() {
        ops.player(player.number()).discard(player.skillCards().cards().toArray(SkillCard[]::new));
        return Followup.NONE;
    }
}
