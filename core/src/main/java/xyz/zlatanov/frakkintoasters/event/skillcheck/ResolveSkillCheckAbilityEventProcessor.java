package xyz.zlatanov.frakkintoasters.event.skillcheck;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.skillCheckAbilityFollowup;

public class ResolveSkillCheckAbilityEventProcessor extends EventProcessor<ResolveSkillCheckAbilityEvent> {

    @Override
    protected boolean isValid() {
        val skillCheck = game.activeSkillCheck();
        // Once per check, and only while a card with the ability is still in the check (rule 10.4.2).
        return event.type() != null
                && event.type().hasSkillCheckAbility()
                && !skillCheck.resolvedAbilities().contains(event.type())
                && skillCheck.dividedCards().stream().anyMatch(card -> card.type() == event.type());
    }

    @Override
    public Followup process() {
        // The abilities that change the strength or the result take effect later, when the check reads them.
        ops.skillCheck().resolveAbility(event.type());
        return skillCheckAbilityFollowup(game.currentPlayer(), event.type());
    }
}
