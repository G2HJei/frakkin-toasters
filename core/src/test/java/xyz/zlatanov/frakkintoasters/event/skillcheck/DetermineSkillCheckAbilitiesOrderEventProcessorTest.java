package xyz.zlatanov.frakkintoasters.event.skillcheck;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.event.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.skillcheck.ability.DamageViperAndRemoveCardFromSkillCheckEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.List;

import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.DOGFIGHT;

class DetermineSkillCheckAbilitiesOrderEventProcessorTest extends EventTestHarness<DetermineSkillCheckAbilitiesOrderEvent> {

    @Test
    void shouldNotFollowupIfNoAbilityCards() {
        execute(new DetermineSkillCheckAbilitiesOrderEvent(1, List.of()));
    }


    @Test
    void shouldFollowupDogfight() {
        execute(new DetermineSkillCheckAbilitiesOrderEvent(1, List.of(
                new SkillCard(0, DOGFIGHT))));

        assertFollowup(one(
                new PlayerDecisionEvent<>(1, DamageViperAndRemoveCardFromSkillCheckEvent.class),
                new NoOpEvent(1)
        ));
    }
}