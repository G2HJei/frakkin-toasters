package xyz.zlatanov.frakkintoasters.event.character;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.HERETIC;

public class HereticDiscardEventProcessor extends EventProcessor<HereticDiscardEvent> {

    @Override
    protected boolean isValid() {
        return event.card() != null && player.superCrisisCards().cards().contains(event.card());
    }

    @Override
    public Followup process() {
        val playerOps = ops.player(player.number());
        playerOps.useAbility(HERETIC);
        playerOps.discardSuperCrisisCard(event.card());
        return NONE;
    }
}
