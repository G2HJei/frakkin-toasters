package xyz.zlatanov.frakkintoasters.event.skillcheck;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import static xyz.zlatanov.frakkintoasters.event.Followup.*;

public class DetermineSkillCheckAbilitiesOrderEventProcessor extends EventProcessor<DetermineSkillCheckAbilitiesOrderEvent> {

    @Override
    public Followup process() {
        val skillCheck = game.activeSkillCheck();
        val abilitiesLeft = event.cardsWithAbilities().stream()
                .filter(SkillCard::hasSkillCheckAbility)
                // "If the current player removes a Skill Card before its skill check ability is resolved, he does not
                // resolve that ability" (rule 10.4.2).
                .filter(skillCheck.dividedCards()::contains)
                .map(SkillCard::type)
                // "Do not resolve the same Skill Check Ability more than once" (rule 10.4.2).
                .filter(type -> !skillCheck.resolvedAbilities().contains(type))
                .distinct()
                .map(type -> single(new ResolveSkillCheckAbilityEvent(type)))
                .toArray(Followup[]::new);
        if (abilitiesLeft.length == 0) {
            return NONE;
        }
        // The current player resolves them "in the order of his choosing", one at a time, so the next pick sees what
        // this one changed.
        return all(one(abilitiesLeft), single(event));
    }
}
