package xyz.zlatanov.frakkintoasters.event.skillcheck;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.skillcheck.ability.DamageViperAndRemoveCardFromSkillCheckEvent;
import xyz.zlatanov.frakkintoasters.event.skillcheck.ability.ForceTheirHandEvent;
import xyz.zlatanov.frakkintoasters.event.skillcheck.ability.QuickThinkingEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.*;

public class DetermineSkillCheckAbilitiesOrderEventProcessor extends EventProcessor<DetermineSkillCheckAbilitiesOrderEvent> {
    @Override
    public Followup process() {
        val followups = event.cardsWithAbilities()
                .stream()
                .map(this::buildFollowup)
                .filter(f -> !f.equals(Followup.NONE))
                .toList();
        return followups.isEmpty()
                ? Followup.NONE
                : Followup.all(followups.toArray(Followup[]::new));
    }

    private Followup buildFollowup(SkillCard skillCard) {
        if (DOGFIGHT == skillCard.type()) {
            return one(
                    new PlayerDecisionEvent<>(player.number(), DamageViperAndRemoveCardFromSkillCheckEvent.class),
                    new NoOpEvent(player.number())
            );
        }
        if (FORCE_THEIR_HAND == skillCard.type() && player.isHuman()) {
            return single(new PlayerDecisionEvent<>(player.number(), ForceTheirHandEvent.class));
        }
        if (QUICK_THINKING == skillCard.type()) {
            return one(
                    new PlayerDecisionEvent<>(player.number(), QuickThinkingEvent.class),
                    new NoOpEvent(player.number())
            );
        }
        return Followup.NONE;
    }
}
