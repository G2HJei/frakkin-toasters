package xyz.zlatanov.frakkintoasters.event.skillcheck;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.event.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.skillcheck.ability.DamageViperAndRemoveCardFromSkillCheckEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.List;

import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint.PLAY_EXACTLY_1;
import static xyz.zlatanov.frakkintoasters.state.character.Character.KARA_STARBUCK_THRACE;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.DOGFIGHT;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.FORCE_THEIR_HAND;

class DetermineSkillCheckAbilitiesOrderEventProcessorTest extends EventTestHarness<DetermineSkillCheckAbilitiesOrderEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(KARA_STARBUCK_THRACE);
    }

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

    @Test
    void shouldFollowupForceTheirHand() {
        execute(new DetermineSkillCheckAbilitiesOrderEvent(1, List.of(
                new SkillCard(0, FORCE_THEIR_HAND))));

        assertFollowup(one(
                new PlayerDecisionEvent<>(1, PlaySkillsEvent.class, List.of(PLAY_EXACTLY_1)),
                new NoOpEvent(1)
        ));
    }

    @Test
    void shouldNotFollowupForceTheirHandWhenCurrentPlayerNonHuman() {
        revealCylon();
        execute(new DetermineSkillCheckAbilitiesOrderEvent(1, List.of(
                new SkillCard(0, FORCE_THEIR_HAND))));
    }
}