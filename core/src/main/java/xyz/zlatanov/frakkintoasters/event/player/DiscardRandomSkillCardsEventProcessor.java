package xyz.zlatanov.frakkintoasters.event.player;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

public class DiscardRandomSkillCardsEventProcessor extends EventProcessor<DiscardRandomSkillCardsEvent> {

    // Not a choice, so "Bait" and "Dradis Contact" don't make the player draw a Mutiny Card (rule A2.3.8.1).
    @Override
    public Followup process() {
        ops.player(player.number()).discardRandomSkillCards(event.count());
        return Followup.NONE;
    }
}
