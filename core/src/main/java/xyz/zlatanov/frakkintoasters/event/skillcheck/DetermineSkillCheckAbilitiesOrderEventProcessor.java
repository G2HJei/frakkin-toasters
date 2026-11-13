package xyz.zlatanov.frakkintoasters.event.skillcheck;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardType;

import java.util.LinkedHashMap;

import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.allOf;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.skillCheckAbilityFollowup;

public class DetermineSkillCheckAbilitiesOrderEventProcessor extends EventProcessor<DetermineSkillCheckAbilitiesOrderEvent> {

    @Override
    public Followup process() {
        // "Do not resolve the same Skill Check Ability more than once" (rule 10.4.2), so one card per type.
        val cardsByType = new LinkedHashMap<SkillCardType, SkillCard>();
        event.cardsWithAbilities().stream()
                .filter(SkillCard::hasSkillCheckAbility)
                .filter(card -> !game.activeSkillCheck().resolvedAbilities().contains(card.type()))
                .forEach(card -> cardsByType.putIfAbsent(card.type(), card));
        // The abilities that change the strength or the result take effect later, when the check reads them.
        cardsByType.keySet().forEach(ops.skillCheck()::resolveAbility);
        return allOf(cardsByType.values().stream()
                .map(card -> skillCheckAbilityFollowup(player, card))
                .toArray(Followup[]::new));
    }
}
