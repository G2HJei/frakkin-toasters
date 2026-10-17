package xyz.zlatanov.frakkintoasters.event.skillcheck;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.FollowupUtils;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

public class DetermineSkillCheckAbilitiesOrderEventProcessor extends EventProcessor<DetermineSkillCheckAbilitiesOrderEvent> {
    @Override
    public Followup process() {
        val followups = event.cardsWithAbilities()
                .stream()
                .filter(SkillCard::hasSkillCheckAbility)
                .map(c -> FollowupUtils.skillCheckAbilityFollowup(player, c))
                .distinct()
                .toList();
        return followups.isEmpty()
                ? Followup.NONE
                : Followup.all(followups.toArray(Followup[]::new));
    }
}
