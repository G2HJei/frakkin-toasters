package xyz.zlatanov.frakkintoasters.event.action.character;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;

public class TreatmentGiveEventProcessor extends EventProcessor<TreatmentGiveEvent> {

    @Override
    protected boolean isValid() {
        return isPlayer(event.targetPlayerNumber()) && event.targetPlayerNumber() != player.number()
                && event.cards() != null
                && event.cards().size() == Math.min(TreatmentEventProcessor.CARDS, player.skillCards().size())
                && holdsSkillCards(event.cards());
    }

    @Override
    public Followup process() {
        ops.player(event.playerNumber()).giveSkillCards(event.targetPlayerNumber(), event.cards());
        return NONE;
    }
}
