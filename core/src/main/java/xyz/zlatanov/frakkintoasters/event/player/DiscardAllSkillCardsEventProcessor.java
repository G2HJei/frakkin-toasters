package xyz.zlatanov.frakkintoasters.event.player;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

public class DiscardAllSkillCardsEventProcessor extends EventProcessor<DiscardAllSkillCardsEvent> {

    // Discarding the whole hand is not a choice, so "Bait" and "Dradis Contact" don't make the player draw a Mutiny Card
    // (rule A2.3.8.1).
    @Override
    public Followup process() {
        val hand = player.skillCards().cards();
        if (!hand.isEmpty()) {
            ops.player(player.number()).discard(hand.toArray(SkillCard[]::new));
        }
        return Followup.NONE;
    }
}
