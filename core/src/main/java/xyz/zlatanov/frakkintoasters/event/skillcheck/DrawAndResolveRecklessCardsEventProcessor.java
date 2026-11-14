package xyz.zlatanov.frakkintoasters.event.skillcheck;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.FollowupUtils;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;

public class DrawAndResolveRecklessCardsEventProcessor extends EventProcessor<DrawAndResolveRecklessCardsEvent> {

    @Override
    public Followup process() {
        val first = ops.skillCheck().revealAndDiscardTreacheryCard();
        if (first.value() > 0) {
            return Followup.NONE;
        }
        val second = ops.skillCheck().revealAndDiscardTreacheryCard();
        return all(abilityFollowup(first), abilityFollowup(second));
    }

    private Followup abilityFollowup(SkillCard card) {
        return FollowupUtils.skillCheckAbilityFollowup(game.currentPlayer(), card.type());
    }
}
